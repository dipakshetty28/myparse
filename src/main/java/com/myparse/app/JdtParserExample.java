package com.myparse.app;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JdtParserExample {

	private static List<String> classNames = new ArrayList<String>();
	private static List<String> variableNames = new ArrayList<String>();
	private static List<String> methodNames = new ArrayList<String>();
	private static List<String> unusedImports = new ArrayList<String>();

	public static void main(String[] args) {
//		if (args.length != 1) {
//			System.out.println("Please provide the path to the Java project directory.");
//			return;
//		}
		File projectDir = new File("C:\\Users\\dipak\\eclipse-workspace\\Testing");
		processDirectory(projectDir);

		// Convert results to JSON and print
		JSONObject result = new JSONObject();
		result.put("nonMatchingClasses", classNames);
		result.put("nonMatchingVariables", variableNames);
		result.put("nonMatchingFunctions", methodNames);
		result.put("unusedImports", unusedImports);
		System.out.println(result.toString());
	}

	public static void processDirectory(File dir) {
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				processDirectory(file);
			} else if (file.getName().endsWith(".java")) {
				try {
					processJavaFile(file);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void processJavaFile(File file) throws IOException {
		String sourceCode = new String(Files.readAllBytes(file.toPath()));

		ASTParser parser = ASTParser.newParser(AST.JLS15); // Adjust as needed
		parser.setKind(ASTParser.K_COMPILATION_UNIT);

		Map<String, String> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_15, options); // Adjust as needed
		parser.setCompilerOptions(options);

		parser.setSource(sourceCode.toCharArray());

		CompilationUnit cu = (CompilationUnit) parser.createAST(null);

		cu.accept(new org.eclipse.jdt.core.dom.ASTVisitor() {
			public boolean visit(org.eclipse.jdt.core.dom.TypeDeclaration node) {
				String className = node.getName().getIdentifier();
				classNames.add(className);
				return true;
			}

			public boolean visit(VariableDeclarationFragment node) {
				String varName = node.getName().getIdentifier();
				variableNames.add(varName);
				return true;
			}


			public boolean visit(MethodDeclaration node) {
				String methodName = node.getName().getIdentifier();
				methodNames.add(methodName);
				return true;
			}

		});

		for (IProblem problem : cu.getProblems()) {
			if (problem.getID() == IProblem.UnusedImport) {
				unusedImports.add(problem.getMessage());
			}
		}
	}
}
