package com.example.arxml.validation.headless;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.validation.model.EvaluationMode;
import org.eclipse.emf.validation.service.IBatchValidator;
import org.eclipse.emf.validation.service.ModelValidationService;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

import autosar40.ecucdescription.EcucdescriptionPackage;
import autosar40.ecucparameterdef.EcucparameterdefPackage;
import autosar40.genericstructure.generaltemplateclasses.arobject.ArobjectPackage;
import autosar40.util.Autosar40ResourceFactoryImpl;

/**
 * Headless IApplication that loads an AUTOSAR arxml file with the artop
 * serialization layer and runs the artop EMF Validation (Batch) constraints
 * (the ECUC 4.0 constraint set), reporting load/validate timings and the
 * number of diagnostics found.
 *
 * Output line (parsed by the benchmark harness):
 *   load_ms=X validate_ms=Y errors=Z warnings=W total_diags=N
 */
public class ArxmlValidationApplication implements IApplication {

    private static final String LINE = "load_ms=%d validate_ms=%d errors=%d warnings=%d total_diags=%d";

    @Override
    public Object start(IApplicationContext context) throws Exception {
        @SuppressWarnings("unchecked")
        String[] args = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
        String input = null;
        for (int i = 0; args != null && i < args.length - 1; i++) {
            if ("-input".equals(args[i])) {
                input = args[i + 1];
            }
        }
        if (input == null || input.isEmpty()) {
            System.err.println("ERROR: missing -input <path> argument");
            return Integer.valueOf(1);
        }
        System.out.println("[headless] input=" + input);

        // --- Defensive metamodel registration: force EMF package init so the
        // EPackage.Registry is populated even outside an editing domain. ---
        try {
            ArobjectPackage.eINSTANCE.eClass();
            EcucdescriptionPackage.eINSTANCE.eClass();
            EcucparameterdefPackage.eINSTANCE.eClass();
            System.out.println("[headless] metamodel packages initialized");
        } catch (Throwable t) {
            System.out.println("[headless] metamodel init warning: " + t);
        }

        // --- Resource set + manual arxml factory registration (the artop
        // factory is normally bound via EMF content-type, which we bypass). ---
        ResourceSet rs = new ResourceSetImpl();
        rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("arxml",
                new Autosar40ResourceFactoryImpl());
        // Also map the * wildcard in case the URI has no extension.
        rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put(Resource.Factory.Registry.DEFAULT_EXTENSION,
                new Autosar40ResourceFactoryImpl());

        URI uri = URI.createFileURI(input);
        long t0 = System.nanoTime();
        Resource resource = rs.createResource(uri);
        java.util.Map<Object, Object> loadOpts = new java.util.HashMap<Object, Object>();
        loadOpts.put(Resource.OPTION_LINE_DELIMITER, "\n");
        try {
            resource.load(loadOpts);
        } catch (Throwable t) {
            // Some options may not be supported; retry plain load.
            System.out.println("[headless] load with options failed, retrying plain: " + t);
            resource.unload();
            resource.load(java.util.Collections.emptyMap());
        }
        long t1 = System.nanoTime();
        long loadMs = Math.round((t1 - t0) / 1_000_000.0);

        if (resource.getContents().isEmpty()) {
            System.err.println("ERROR: resource has no contents after load");
            System.out.println(String.format(LINE, loadMs, 0L, 0, 0, 0));
            return Integer.valueOf(2);
        }
        EObject root = resource.getContents().get(0);
        int objCount = 0;
        for (java.util.Iterator<EObject> it = root.eAllContents(); it.hasNext(); it.next()) {
            objCount++;
        }
        System.out.println("[headless] root=" + root.eClass().getName() + " objects=" + (objCount + 1));

        // --- Batch validation via EMF Validation service (same entry point
        // used by Sphinx' EValidatorAdapter). ---
        @SuppressWarnings("rawtypes")
        IBatchValidator validator = (IBatchValidator) ModelValidationService.getInstance()
                .newValidator(EvaluationMode.BATCH);
        validator.setIncludeLiveConstraints(true);
        validator.setReportSuccesses(false);

        long t2 = System.nanoTime();
        IStatus status;
        try {
            status = validator.validate(root, new NullProgressMonitor());
        } catch (Throwable t) {
            System.out.println("[headless] validate(EObject,monitor) failed, retrying validate(EObject): " + t);
            status = validator.validate(root);
        }
        long t3 = System.nanoTime();
        long validateMs = Math.round((t3 - t2) / 1_000_000.0);

        int[] counts = count(status);
        int errors = counts[0];
        int warnings = counts[1];
        int total = counts[2];

        List<IStatus> leaves = new ArrayList<IStatus>();
        collectLeaves(status, leaves);

        System.out.println(String.format(LINE, loadMs, validateMs, errors, warnings, total));
        System.out.println("[headless] status severity=" + severityName(status.getSeverity()) + " children="
                + (status.isMultiStatus() ? status.getChildren().length : 0));
        int shown = 0;
        for (IStatus s : leaves) {
            if (s.getSeverity() == IStatus.ERROR || s.getSeverity() == IStatus.WARNING) {
                System.out.println("  [" + severityName(s.getSeverity()) + "] code=" + s.getCode() + " "
                        + truncate(s.getMessage(), 200));
                shown++;
                if (shown >= 20) {
                    break;
                }
            }
        }
        return Integer.valueOf(0);
    }

    /** Returns {errors, warnings, total_non_ok}. */
    private static int[] count(IStatus status) {
        int[] c = new int[3];
        countRec(status, c);
        return c;
    }

    private static void countRec(IStatus status, int[] c) {
        IStatus[] children = status.getChildren();
        if (children == null || children.length == 0) {
            // leaf
            if (status.getSeverity() == IStatus.ERROR) {
                c[0]++;
            } else if (status.getSeverity() == IStatus.WARNING) {
                c[1]++;
            }
            if (status.getSeverity() != IStatus.OK) {
                c[2]++;
            }
            return;
        }
        for (IStatus ch : children) {
            countRec(ch, c);
        }
    }

    private static void collectLeaves(IStatus status, List<IStatus> out) {
        IStatus[] children = status.getChildren();
        if (children == null || children.length == 0) {
            out.add(status);
            return;
        }
        for (IStatus ch : children) {
            collectLeaves(ch, out);
        }
    }

    private static String severityName(int sev) {
        switch (sev) {
        case IStatus.ERROR:
            return "ERROR";
        case IStatus.WARNING:
            return "WARNING";
        case IStatus.INFO:
            return "INFO";
        case IStatus.CANCEL:
            return "CANCEL";
        case IStatus.OK:
            return "OK";
        default:
            return String.valueOf(sev);
        }
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        String one = s.replace('\n', ' ').replace('\r', ' ');
        if (one.length() <= max) {
            return one;
        }
        return one.substring(0, max) + "...";
    }

    @Override
    public void stop() {
        // no-op
    }
}
