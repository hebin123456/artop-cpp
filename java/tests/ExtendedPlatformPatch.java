package org.eclipse.sphinx.platform.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.osgi.framework.Bundle;

/**
 * 替换版 ExtendedPlatform，修复非 OSGi 环境下的 NPE 问题。
 *
 * 原始实现中 getFeatureVersion() 调用 Platform.getBundle() 在非 OSGi 环境下会触发 NPE
 * （因为 InternalPlatform.fwkWiring 为 null）。
 *
 * 本替换类修复了 getFeatureVersion() 和 getFeatureVersionOrdinal() 方法，
 * 在非 OSGi 环境下返回安全默认值，避免 NPE 和 NumberFormatException。
 *
 * 其他方法提供桩实现，因为它们仅在 Eclipse 工作区环境下使用，非 OSGi 环境不会调用。
 */
public final class ExtendedPlatform {

	public static final int LIMIT_INDIVIDUAL_RESOURCES_SCHEDULING_RULE = 1;
	public static final boolean IS_ECLIPSE_RUNNING;
	public static final boolean IS_RESOURCES_BUNDLE_AVAILABLE;
	private static final String NO_CONTENT_TYPE_ID = "no-content-type-id"; //$NON-NLS-1$

	static {
		boolean eclipseRunning = false;
		try {
			eclipseRunning = Platform.isRunning();
		} catch (Throwable t) {
			// 非 OSGi 环境下 Platform.isRunning() 可能抛异常，忽略
		}
		IS_ECLIPSE_RUNNING = eclipseRunning;
		IS_RESOURCES_BUNDLE_AVAILABLE = false; // 非 OSGi 环境下资源包不可用
	}

	private ExtendedPlatform() {
	}

	/**
	 * 获取 Eclipse 核心运行时特性版本。
	 * 修复：在非 OSGi 环境下捕获异常，返回空字符串而非触发 NPE。
	 */
	public static String getFeatureVersion() {
		try {
			Bundle bundle = Platform.getBundle("org.eclipse.core.runtime"); //$NON-NLS-1$
			if (bundle != null) {
				@SuppressWarnings("unchecked")
				Dictionary<String, String> headers = bundle.getHeaders();
				String version = headers.get("Bundle-Version"); //$NON-NLS-1$
				if (version != null && version.length() >= 3) {
					return version.substring(0, 3);
				}
			}
		} catch (Throwable t) {
			// 非 OSGi 环境下 Platform.getBundle() 可能抛 NPE，忽略
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * 获取 Eclipse 核心运行时特性版本序数。
	 * 修复：在版本字符串为空时返回 0，避免 NumberFormatException。
	 */
	public static int getFeatureVersionOrdinal() {
		String version = getFeatureVersion();
		if (version == null || version.isEmpty()) {
			return 0;
		}
		try {
			return Integer.parseInt(version.replaceAll("\\.", "")); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public static boolean isBundleAvailable(String bundleId) {
		if (!IS_ECLIPSE_RUNNING) {
			return false;
		}
		try {
			Bundle bundle = Platform.getBundle(bundleId);
			if (bundle != null) {
				return (bundle.getState() & (Bundle.RESOLVED | Bundle.STARTING | Bundle.ACTIVE | Bundle.STOPPING)) != 0;
			}
		} catch (Throwable t) {
			// 忽略
		}
		return false;
	}

	public static Bundle loadBundle(String bundleId) {
		if (!IS_ECLIPSE_RUNNING) {
			return null;
		}
		try {
			return Platform.getBundle(bundleId);
		} catch (Throwable t) {
			return null;
		}
	}

	public static final Bundle loadContributorBundle(org.eclipse.core.runtime.IConfigurationElement element) {
		return null;
	}

	// ===== 调度规则方法（非 OSGi 环境不需要）=====
	public static ISchedulingRule createModifySchedulingRule(IResource resource) {
		return null;
	}

	public static ISchedulingRule createModifySchedulingRule(Collection<IFile> files) {
		return null;
	}

	public static ISchedulingRule createCreateSchedulingRule(IResource resource) {
		return null;
	}

	public static ISchedulingRule createSaveNewSchedulingRule(IResource resource) {
		return null;
	}

	public static ISchedulingRule createSaveNewSchedulingRule(IPath path) {
		return null;
	}

	public static ISchedulingRule createSaveNewSchedulingRule(Collection<IPath> paths) {
		return null;
	}

	public static ISchedulingRule createSaveSchedulingRule(IResource resource) {
		return null;
	}

	// ===== 项目/资源判断方法 =====
	public static boolean isProjectDescriptionFile(IResource resource) {
		return false;
	}

	public static boolean isProjectPropertiesFolder(IResource resource) {
		return false;
	}

	public static boolean isProjectPropertiesFile(IResource resource) {
		return false;
	}

	public static boolean isTeamPrivateResource(IResource resource) {
		return false;
	}

	public static boolean isTeamPrivateResource(IResource resource, int flags) {
		return false;
	}

	public static boolean isPlatformPrivateResource(IResource resource) {
		return false;
	}

	// ===== 文件收集方法 =====
	public static final Collection<IFile> getAllFiles(IFolder folder) {
		return Collections.emptyList();
	}

	private static void collectAllFiles(IProject project, IResource[] members,
			java.util.List<IFile> files, Collection<IProject> visitedProjects, boolean includeTeamPrivate) {
		// 空实现
	}

	public static final Collection<IFile> getAllFiles(IProject project, boolean includeTeamPrivate) {
		return Collections.emptyList();
	}

	public static final IResource[] getMembersSafely(IContainer container) {
		return new IResource[0];
	}

	public static IProject[] getReferencedProjectsSafely(IProject project) {
		return new IProject[0];
	}

	public static IProject[] getReferencingProjectsSafely(IProject project) {
		return new IProject[0];
	}

	private static void collectProjectsInGroup(IProject project, boolean includeReferencing,
			Set<IProject> projects) {
		// 空实现
	}

	public static Set<IProject> getProjectGroup(IProject project, boolean includeReferencing) {
		return Collections.emptySet();
	}

	private static void collectReferencedProjects(IProject project, Set<IProject> projects) {
		// 空实现
	}

	public static Collection<IProject> getAllReferencedProjects(IProject project) {
		return Collections.emptyList();
	}

	private static void collectReferencingProjects(IProject project, Set<IProject> projects) {
		// 空实现
	}

	public static Collection<IProject> getAllReferencingProjects(IProject project) {
		return Collections.emptyList();
	}

	public static boolean isRootProject(IProject project) {
		return false;
	}

	public static IProject getFirstRootProject(IProject project) {
		return null;
	}

	private static void collectRootProjects(Collection<IProject> rootProjects,
			Collection<IProject> visitedProjects, IProject project) {
		// 空实现
	}

	public static Collection<IProject> getRootProjects() {
		return Collections.emptyList();
	}

	public static Collection<IProject> getProjects(String contentType) {
		return Collections.emptyList();
	}

	// ===== 唯一文件名/路径方法 =====
	public static String createUniqueFileName(IPath path, String extension) {
		return null;
	}

	public static IPath createUniquePath(IPath path, Collection<IPath> existingPaths) {
		return null;
	}

	// ===== 内容类型方法 =====
	public static String getContentTypeId(IFile file) throws org.eclipse.core.runtime.CoreException {
		return null;
	}

	public static String getContentTypeId(java.io.File file) throws IOException {
		return null;
	}

	private static String nativeGetContentTypeId(IFile file) throws org.eclipse.core.runtime.CoreException {
		return null;
	}

	public static boolean hasCachedContentTypeId(IFile file) {
		return false;
	}

	private static String getCachedContentTypeId(IFile file) {
		return null;
	}

	private static String internalGetCachedContentTypeId(IFile file) {
		return null;
	}

	public static void setCachedContentTypeId(IFile file, String contentTypeId) {
		// 空实现
	}

	private static void internalSetCachedContentTypeId(IFile file, String contentTypeId) {
		// 空实现
	}

	public static void persistContentTypeIdProperties(IProject project, boolean force,
			boolean includeTeamPrivate, IProgressMonitor monitor) {
		// 空实现
	}

	public static void persistContentTypeIdProperties(Collection<IFile> files, boolean force,
			IProgressMonitor monitor) {
		// 空实现
	}

	private static void runPersistContentTypeIdProperties(Collection<IFile> files,
			IProgressMonitor monitor) throws org.eclipse.core.runtime.OperationCanceledException {
		// 空实现
	}

	public static void removeCachedContentTypeId(IFile file) {
		// 空实现
	}

	public static Collection<String> getContentTypeFileExtensions(String contentTypeId) {
		return Collections.emptyList();
	}

	public static boolean isContentTypeApplicable(String contentTypeId, IFile file) {
		return false;
	}

	public static QualifiedName toQualifedName(String qualifiedName) {
		return null;
	}

	// ===== Nature 方法 =====
	public static void addNature(IProject project, String natureId, IProgressMonitor monitor)
			throws org.eclipse.core.runtime.CoreException {
		// 空实现
	}

	public static void removeNature(IProject project, String natureId, IProgressMonitor monitor)
			throws org.eclipse.core.runtime.CoreException {
		// 空实现
	}

	// ===== 安全关闭方法（提供实际实现）=====
	public static void safeClose(InputStream inputStream) {
		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (IOException e) {
				// 忽略关闭异常
			}
		}
	}

	public static void safeClose(OutputStream outputStream) {
		if (outputStream != null) {
			try {
				outputStream.close();
			} catch (IOException e) {
				// 忽略关闭异常
			}
		}
	}

	// ===== 其他方法 =====
	public static final void performGarbageCollection() {
		System.gc();
	}

	public static boolean isSynchronized(IResource resource) {
		return true;
	}

	// 包内访问方法（供内部类调用）
	static void access$0(Collection<IFile> files, IProgressMonitor monitor)
			throws org.eclipse.core.runtime.OperationCanceledException {
		runPersistContentTypeIdProperties(files, monitor);
	}
}
