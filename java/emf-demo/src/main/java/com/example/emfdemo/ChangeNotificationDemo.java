package com.example.emfdemo;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EContentAdapter;

/** * 演示 EMF 的变更通知（Change Notification）机制。
 *
 * 关键 API：
 *   - Notifier  : 所有可被监听的 EMF 对象（EObject、Resource、ResourceSet）
 *   - Adapter   : 监听者接口
 *   - EContentAdapter : 递归监听整个 containment 树
 *   - Notification: 事件对象，包含 eventType / feature / oldValue / newValue 等
 */
public final class ChangeNotificationDemo {

    private ChangeNotificationDemo() {}

    public static void run() {
        System.out.println("\n========== Demo 3: 变更通知事件 ==========");

        EPackage libraryPkg = EcoreModelBuilder.build();
        EObject library = DynamicInstanceFactory.createSampleLibrary(libraryPkg);
        EClass libraryCls = library.eClass();
        EReference booksRef = (EReference) libraryCls.getEStructuralFeature("books");
        EReference authorsRef = (EReference) libraryCls.getEStructuralFeature("authors");
        EAttribute nameAttr = (EAttribute) libraryCls.getEStructuralFeature("name");

        // ---------- 1) 普通 Adapter：只监听 library 自身的变更 ----------
        System.out.println("\n--- Part A: Plain Adapter on Library ---");
        library.eAdapters().add(new LabeledAdapter("[Library-Adapter]"));

        library.eSet(nameAttr, "City Central Library (Renamed)");   // SET 事件
        EList<EObject> books = (EList<EObject>) library.eGet(booksRef);
        EObject firstBook = books.get(0);
        EObject newBook = libraryPkg.getEFactoryInstance().create((EClass) libraryPkg.getEClassifier("Book"));
        newBook.eSet(newBook.eClass().getEStructuralFeature("title"), "New Arrival");
        books.add(newBook);                                          // ADD 事件
        books.remove(firstBook);                                     // REMOVE 事件

        // ---------- 2) EContentAdapter：递归监听整个树 ----------
        System.out.println("\n--- Part B: EContentAdapter (recursive) ---");
        Adapter contentAdapter = new EContentAdapter() {
            @Override
            public void notifyChanged(Notification msg) {
                super.notifyChanged(msg);
                System.out.println(format("EContentAdapter", msg));
            }
        };
        library.eAdapters().add(contentAdapter);

        // 触发：修改深层 book 的属性
        EObject target = ((EList<EObject>) library.eGet(booksRef)).get(0);
        EAttribute titleAttr = (EAttribute) target.eClass().getEStructuralFeature("title");
        EAttribute priceAttr = (EAttribute) target.eClass().getEStructuralFeature("price");
        target.eSet(titleAttr, "The Pragmatic Programmer (2nd Edition)");
        target.eSet(priceAttr, 59.99);

        // 触发：深层 add 一本 magazine
        EClass magCls = (EClass) libraryPkg.getEClassifier("Magazine");
        EObject mag = libraryPkg.getEFactoryInstance().create(magCls);
        mag.eSet(magCls.getEStructuralFeature("title"), "Scientific American");
        ((EList<EObject>) library.eGet(libraryCls.getEStructuralFeature("magazines"))).add(mag);

        // ---------- 3) 类型安全的自定义 Adapter：只关心特定 feature ----------
        System.out.println("\n--- Part C: Type-safe Adapter for 'name' changes only ---");
        EObject specificAuthor = ((EList<EObject>) library.eGet(authorsRef)).get(0);
        specificAuthor.eAdapters().add(new EAttributeOnlyAdapter());

        // 修改 Author.name —— 应触发
        EAttribute authorName = (EAttribute) specificAuthor.eClass().getEStructuralFeature("name");
        specificAuthor.eSet(authorName, "Ada Lovelace (Updated)");

        // 修改 Author.email —— 不应触发（因为只监听了 name）
        EAttribute authorEmail = (EAttribute) specificAuthor.eClass().getEStructuralFeature("email");
        specificAuthor.eSet(authorEmail, "ada-updated@example.com");

        // ---------- 4) 移除 listener ----------
        System.out.println("\n--- Part D: Remove adapter (no more notifications) ---");
        library.eAdapters().clear();
        library.eSet(nameAttr, "Should NOT print anything");
        System.out.println("[ChangeNotificationDemo] Done.");
    }

    private static String format(String tag, Notification msg) {
        Object feature = msg.getFeature();
        String featName = "<no-feature>";
        if (feature instanceof org.eclipse.emf.ecore.EStructuralFeature) {
            featName = ((org.eclipse.emf.ecore.EStructuralFeature) feature).getName();
        } else if (feature != null) {
            featName = feature.toString();
        }
        Object notifier = msg.getNotifier();
        String notifierName = notifier == null ? "null"
                : (notifier instanceof EObject
                        ? ((EObject) notifier).eClass().getName()
                        : notifier.getClass().getSimpleName());
        return String.format("  [%s] notifier=%s feature=%s type=%s old=%s new=%s position=%s",
                tag,
                notifierName,
                featName,
                eventTypeName(msg.getEventType()),
                trim(msg.getOldValue()),
                trim(msg.getNewValue()),
                msg.getPosition());
    }

    private static String trim(Object v) {
        if (v == null) return "null";
        String s = v.toString();
        return s.length() > 40 ? s.substring(0, 40) + "..." : s;
    }

    private static String eventTypeName(int type) {
        switch (type) {
            case Notification.SET: return "SET";
            case Notification.UNSET: return "UNSET";
            case Notification.ADD: return "ADD";
            case Notification.ADD_MANY: return "ADD_MANY";
            case Notification.REMOVE: return "REMOVE";
            case Notification.REMOVE_MANY: return "REMOVE_MANY";
            case Notification.MOVE: return "MOVE";
            case Notification.REMOVING_ADAPTER: return "REMOVING_ADAPTER";
            case Notification.RESOLVE: return "RESOLVE";
            default: return "EVT(" + type + ")";
        }
    }

    /** 简单 Adapter：打印所有事件。 */
    private static class LabeledAdapter implements Adapter {
        private final String label;
        LabeledAdapter(String label) { this.label = label; }
        @Override public void notifyChanged(Notification msg) {
            System.out.println(format(label, msg));
        }
        @Override public Notifier getTarget() { return null; }
        @Override public void setTarget(Notifier newTarget) {}
        @Override public boolean isAdapterForType(Object type) { return false; }
    }

    /** 类型敏感 Adapter：只关心 'name' 这个 attribute 的变更。 */
    private static class EAttributeOnlyAdapter implements Adapter {
        private Notifier target;
        EAttributeOnlyAdapter() {}
        @Override public void notifyChanged(Notification msg) {
            Object feature = msg.getFeature();
            if (msg.getNotifier() instanceof EObject && feature instanceof EAttribute
                    && "name".equals(((EAttribute) feature).getName())) {
                EObject eo = (EObject) msg.getNotifier();
                System.out.println("  [Name-Only] " + eo.eClass().getName()
                        + " name changed: '" + msg.getOldValue() + "' -> '" + msg.getNewValue() + "'");
            }
        }
        @Override public Notifier getTarget() { return target; }
        @Override public void setTarget(Notifier newTarget) { this.target = newTarget; }
        @Override public boolean isAdapterForType(Object type) { return false; }
    }
}
