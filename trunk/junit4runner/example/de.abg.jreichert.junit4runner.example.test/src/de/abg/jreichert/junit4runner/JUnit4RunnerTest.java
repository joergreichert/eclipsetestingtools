package de.abg.jreichert.junit4runner;

import static org.eclipse.swtbot.swt.finder.waits.Conditions.shellCloses;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.matchers.WithPartName;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.results.WidgetResult;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;
import org.eclipse.ui.IEditorReference;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public class JUnit4RunnerTest {

	private static SWTWorkbenchBot bot;

	@BeforeClass
	public static void beforeClass() throws Exception {
		bot = new SWTWorkbenchBot();
		try {
			bot.viewByTitle("Welcome").close();
		} catch (Exception e) {
			System.err.println("no welcome screen found");
		}
	}

	@Test
	public void testJUnit4TestSuiteCreation() throws Exception {
		bot.perspectiveByLabel("Java").activate();

		String projectName = "MyFirstProject";
		String packageName = "my.firstproject";

		// createPluginProject(projectName);
		createJavaProject(projectName);
		setupAdderClass(projectName, packageName);
		setupSubtracterClass(projectName, packageName);

		// createFragmentTestProject(projectName);
		createJavaTestProject(projectName);
		setupAdderTestClass(projectName, packageName);
		setupSubtracterTestClass(projectName, packageName);

		createJUnit4TestSuite(projectName, packageName);
		executeJUnit4TestSuiteInUi(projectName, packageName);
		try {
			executeJUnit4TestSuiteByFile(projectName, packageName);
		} catch (AssertionError ae) {
			System.err.println(ae.getMessage());
		}
	}

	private void createJavaProject(String projectName) {
		bot.menu("File").menu("New").menu("Project...").click();
		bot.shell("New Project").activate();
		bot.tree().expandNode("Java").select("Java Project");
		bot.button("Next >").click();
		bot.textWithLabel("Project name:").setText(projectName);
		bot.button("Finish").click();
	}

	@SuppressWarnings("unused")
	private void createPluginProject(String projectName) {
		bot.menu("File").menu("New").menu("Project...").click();
		bot.shell("New Project").activate();
		bot.tree().expandNode("Plug-in Development").select("Plug-in Project");
		bot.button("Next >").click();
		bot.textWithLabel("Project name:").setText(projectName);
		bot.button("Next >").click();
		bot.button("Finish").click();
		bot.button("No").click();
	}

	private void setupAdderClass(String projectName, String packageName) {
		String className = "Adder";
		String methodName = "add";
		String parametersStr = "int element1, int element2";
		String methodBody = "return element1 + element2;";
		String methodReturnType = "int";
		setupClassToTest(projectName, packageName, className, methodName,
				parametersStr, methodBody, methodReturnType);
	}

	private void setupClassToTest(String projectName, String packageName,
			String className, String methodName, String parametersStr,
			String methodBody, String methodReturnType) {
		createClass(projectName, packageName, className);
		String classText = getClassText(packageName, className, methodName,
				parametersStr, methodBody, methodReturnType);
		setTextInActiveEditor(className + ".java", classText);
	}

	private void createClass(String projectName, String packageName,
			String className) {
		createClass(projectName, "Class", packageName, className);
	}

	private void createClass(String projectName, String classType,
			String packageName, String className) {
		SWTBot localBot = bot.viewByTitle("Package Explorer").bot();
		localBot.tree().expandNode(projectName).select("src");
		bot.menu("File").menu("New").menu(classType).click();
		bot.textWithLabel("Package:").setText(packageName);
		bot.textWithLabel("Name:").setText(className);
		bot.button("Finish").click();
	}

	private String getClassText(String packageName, String className,
			String methodName, String parametersStr, String methodBody,
			String methodReturnType) {
		return getText("package " + packageName + ";",

		"public class " + className + "{",

		"public " + methodReturnType + " " + methodName + "(" + parametersStr
				+ ") {", methodBody, "}", "}");
	}

	private String getText(String... lines) {
		StringBuilder sb = new StringBuilder();
		for (String line : lines) {
			sb.append(line).append("\n");
		}
		return sb.toString();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void setTextInActiveEditor(String editorName, String text) {
		Matcher withPartName = WithPartName.withPartName(editorName);
		bot.waitUntil(Conditions.waitForEditor(withPartName));
		SWTBotEditor editor = bot.activeEditor();
		SWTBotEclipseEditor textEditor = editor.toTextEditor();
		bot.menu("Edit").menu("Select All").click();
		textEditor.setText(text);
		textEditor.saveAndClose();
	}

	private void setupSubtracterClass(String projectName, String packageName) {
		String className = "Subtracter";
		String methodName = "subtract";
		String parametersStr = "int element1, int element2";
		String methodBody = "return element1 - element2;";
		String methodReturnType = "int";
		setupClassToTest(projectName, packageName, className, methodName,
				parametersStr, methodBody, methodReturnType);
	}

	private void createJavaTestProject(String projectName) {
		bot.menu("File").menu("New").menu("Project...").click();
		SWTBotShell shell = bot.shell("New Project").activate();
		bot.tree().expandNode("Java").select("Java Project");
		bot.button("Next >").click();
		bot.textWithLabel("Project name:").setText(projectName + "Test");
		bot.button("Finish").click();
		bot.waitUntil(shellCloses(shell));
		setupProjectDependency(projectName);
	}

	private void setupProjectDependency(String projectName) {
		bot.menu("Window").menu("Show View").menu("Navigator").click();
		SWTBot localBot = bot.viewByTitle("Navigator").bot();
		SWTBotTree tree = localBot.tree();
		SWTBotTreeItem node = tree.expandNode(projectName + "Test");
		node = node.expandNode(".classpath").select();

		final SWTBotContextMenu cmenu = new SWTBotContextMenu(tree);
		bot.performWithTimeout(new VoidResult() {

			@Override
			public void run() {
				cmenu.click("Open With").click("Text Editor");
			}
		}, 5000);

		Matcher<IEditorReference> withPartName = WithPartName
				.withPartName(Matchers.containsString("classpath"));
		try {
			bot.waitUntil(Conditions.waitForEditor(withPartName));
		} catch (TimeoutException toe) {
			node.doubleClick();
			bot.waitUntil(Conditions.waitForEditor(withPartName));
		}

		String classpathText = getClassPathText(projectName);

		SWTBotEditor editor = bot.activeEditor();
		SWTBotEclipseEditor textEditor = editor.toTextEditor();
		bot.menu("Edit").menu("Select All").click();
		textEditor.setText(classpathText);
		textEditor.saveAndClose();

		bot.viewByTitle("Package Explorer").show();
	}

	public SWTBotMenu getSubMenuItemContainingTest(final SWTBotMenu parentMenu,
			final String itemText) throws WidgetNotFoundException {

		MenuItem menuItem = UIThreadRunnable
				.syncExec(new WidgetResult<MenuItem>() {
					public MenuItem run() {
						Menu menu = parentMenu.widget.getMenu();
						if (menu != null) {
							for (MenuItem item : menu.getItems()) {
								if (item.getText().contains(itemText)) {
									return item;
								}
							}
						}
						return null;
					}
				});

		if (menuItem == null) {
			throw new WidgetNotFoundException("MenuItem \"" + itemText
					+ "\" not found.");
		} else {
			return new SWTBotMenu(menuItem);
		}
	}

	private String getClassPathText(String projectName) {
		return getText(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
				"<classpath>",
				"<classpathentry kind=\"src\" path=\"src\"/>",
				"<classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.6\"/>",
				"<classpathentry kind=\"con\" path=\"org.eclipse.jdt.junit.JUNIT_CONTAINER/4\"/>",
				"<classpathentry combineaccessrules=\"false\" kind=\"src\" path=\"/"
						+ projectName + "\"/>",
				"<classpathentry kind=\"output\" path=\"bin\"/>",
				"</classpath>");
	}

	@SuppressWarnings("unused")
	private void createFragmentTestProject(String projectName) {
		bot.menu("File").menu("New").menu("Project...").click();
		bot.shell("New Project").activate();
		bot.tree().expandNode("Plug-in Development").select("Fragment Project");
		bot.button("Next >").click();
		bot.textWithLabel("Project name:").setText(projectName + "Test");
		bot.button("Next >").click();
		bot.textWithLabel("Plug-in ID:").setText(projectName);
		bot.button("Finish").click();
		bot.button("No").click();
	}

	private void setupAdderTestClass(String projectName, String packageName) {
		String className = "Adder";
		String methodName = "add";
		String testParametersStr = "1, 2";
		String expectedResultStr = "3";
		setupTestClass(projectName, packageName, className, methodName,
				testParametersStr, expectedResultStr);
	}

	private void setupSubtracterTestClass(String projectName, String packageName) {
		String className = "Subtracter";
		String methodName = "subtract";
		String testParametersStr = "3, 2";
		String expectedResultStr = "1";
		setupTestClass(projectName, packageName, className, methodName,
				testParametersStr, expectedResultStr);
	}

	private void setupTestClass(String projectName, String packageName,
			String className, String methodName, String testParametersStr,
			String expectedResultStr) {
		createJUnitTestClass(projectName, packageName, className);
		try {
			bot.button("OK").click();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		String testClassText = getTestClassText(packageName, className,
				methodName, testParametersStr, expectedResultStr);
		setTextInActiveEditor(className + "Test.java", testClassText);
	}

	private void createJUnitTestClass(String projectName, String packageName,
			String className) {
		createClass(projectName + "Test", "JUnit Test Case", packageName,
				className + "Test");
	}

	private String getTestClassText(String packageName, String className,
			String methodName, String parametersStr, String expectedResultStr) {
		return getText("package " + packageName + ";",

		"import org.junit.Assert;", "import org.junit.Test;",

		"import " + packageName + "." + className + ";",

		"public class " + className + "Test {",

		"@Test", "public void test_" + methodName + "() {", className
				+ " fixture = new " + className + "();",
				"Object result = fixture." + methodName + "(" + parametersStr
						+ ");", "Assert.assertEquals(\"expected result\", "
						+ expectedResultStr + ", result);", "}", "}");
	}

	private void createJUnit4TestSuite(String projectName, String packageName) {
		SWTBot localBot = bot.viewByTitle("Package Explorer").bot();
		localBot.tree().expandNode(projectName + "Test", "src")
				.select(packageName);
		bot.menu("File").menu("New").menu("Other...").click();
		bot.shell("New").activate();
		bot.tree().expandNode("Java", "JUnit 4").select("JUnit 4 Test Suite");
		bot.button("Next >").click();
		bot.button("Finish").click();
	}

	private void executeJUnit4TestSuiteInUi(String projectName,
			String packageName) {
		SWTBot localBot = bot.viewByTitle("Package Explorer").bot();
		localBot.tree().expandNode(projectName + "Test", "src", packageName)
				.select("AllTests.java");
		SWTBotMenu menu = bot.menu("Run").menu("Run As");
		try {
			getSubMenuItemContainingTest(menu, "JUnit Test");
		} catch (WidgetNotFoundException wnfe) {
			System.err.println(wnfe.getMessage());
		}
	}

	private void executeJUnit4TestSuiteByFile(String projectName,
			String packageName) {
		try {
			IProject project = ResourcesPlugin.getWorkspace().getRoot()
					.getProject(projectName + "Test");
			IJavaProject javaProject = JavaCore.create(project);
			String[] classPathEntries = JavaRuntime
					.computeDefaultRuntimeClassPath(javaProject);
			List<URL> urlList = new ArrayList<URL>();
			for (int i = 0; i < classPathEntries.length; i++) {
				String entry = classPathEntries[i];
				IPath path = new Path(entry);
				URL url = path.toFile().toURI().toURL();
				urlList.add(url);
			}
			ClassLoader parentClassLoader = project.getClass().getClassLoader();
			URL[] urls = (URL[]) urlList.toArray(new URL[urlList.size()]);
			URLClassLoader classLoader = new URLClassLoader(urls,
					parentClassLoader);
			Class<?> clazz = classLoader.loadClass(packageName + ".AllTests");
			Annotation[] annotations = clazz.getAnnotations();
			Assert.assertEquals(2, annotations.length);
			List<String> annoStrs = new ArrayList<String>();
			for (Annotation anno : annotations) {
				annoStrs.add(anno.toString());
			}
			Assert.assertTrue(annoStrs
					.contains("@org.junit.runners.Suite$SuiteClasses("
							+ "value=[class my.firstproject.SubtracterTest, "
							+ "class my.firstproject.AdderTest])"));
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("exception: " + e.getMessage());
		}
	}

	@AfterClass
	public static void sleep() {
		bot.sleep(2000);
	}
}
