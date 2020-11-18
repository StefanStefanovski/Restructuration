package analyse;

// import org.eclipse.core.resources.IProject;
// import org.eclipse.core.resources.IWorkspace;
// import org.eclipse.core.resources.IWorkspaceRoot;
// import org.eclipse.core.resources.ResourcesPlugin;
// import org.eclipse.jdt.core.dom.AST;
// import org.eclipse.jdt.core.dom.ASTParser;
// import org.eclipse.jdt.core.dom.ASTVisitor;
// import org.eclipse.jdt.core.dom.ASTNode;
// import org.eclipse.jdt.core.dom.CompilationUnit;
// import org.eclipse.jdt.core.dom.FieldDeclaration;
// import org.eclipse.jdt.core.dom.MethodDeclaration;
// import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
// import org.eclipse.jdt.core.dom.TypeDeclaration;
// import org.eclipse.jdt.core.dom.ReturnStatement;
// import org.eclipse.jdt.core.dom.SimpleName;
// import org.eclipse.jdt.core.dom.Block;
// import org.eclipse.jdt.core.dom.Type;
// import org.eclipse.jdt.core.dom.FieldAccess;
// import org.eclipse.jdt.core.dom.Assignment;
// import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
// import org.eclipse.jface.text.Document;
// import org.eclipse.text.edits.TextEdit;

// public class JDTMain {
// 	private static final String path = "output.java";
    
// 	public static void main(String[] args) {
        
//         ASTParser parser = ASTParser.newParser(AST.JLS3); 
// 		parser.setKind(ASTParser.K_COMPILATION_UNIT);
// //		parser.setSource("public class A { int i = 9;  \\n int j; \\n ArrayList<Integer> al = new ArrayList<Integer>();j=1000; }".toCharArray()); // set source
// 		Document document = new Document(ClassAsString.classAsString);
// 		parser.setSource(document.get().toCharArray()); // set source
// 		parser.setResolveBindings(true); // we need bindings later on
// 		CompilationUnit cu = (CompilationUnit) parser.createAST(null /* IProgressMonitor */); // parse
// 		cu.recordModifications();
// 		AST ast = cu.getAST();
		
// 		cu.accept(new ASTVisitor() {
// 			public boolean visit(FieldDeclaration fieldDeclaration) {
// 				String fieldName = fieldDeclaration.fragments().get(0).toString();
// 				MethodDeclaration getter = ast.newMethodDeclaration();
// 				TypeDeclaration parent = (TypeDeclaration) fieldDeclaration.getParent();
// 				Type type = (Type) (ASTNode.copySubtree(ast, fieldDeclaration.getType()).getRoot());
// 				getter.setReturnType2(type);
				
// 				// getter
// 				getter.setName(ast.newSimpleName("get" + capitalize(fieldName)));
// 				Block getterBody = ast.newBlock();
// 				ReturnStatement getterReturnStatement = ast.newReturnStatement();
// 				getterReturnStatement.setExpression(ast.newSimpleName(fieldName));
// 				getterBody.statements().add(getterReturnStatement);
// 				getter.setBody(getterBody);
				
// 				parent.bodyDeclarations().add(getter);

// 				// setter
// 				MethodDeclaration setter = ast.newMethodDeclaration();
// 				setter.setName(ast.newSimpleName("set" + capitalize(fieldName)));
// 				SingleVariableDeclaration parameter = ast.newSingleVariableDeclaration();
// 				parameter.setName(ast.newSimpleName(fieldName));
// 				type = (Type) (ASTNode.copySubtree(ast, fieldDeclaration.getType()).getRoot());
// 				parameter.setType(type);
// 				setter.parameters().add(parameter);
// 				Block setterBody = ast.newBlock();
// 				FieldAccess field = ast.newFieldAccess();
// 				field.setExpression(ast.newThisExpression());
// 				field.setName(ast.newSimpleName(fieldName));
// 				Assignment assignment = ast.newAssignment();
// 				assignment.setLeftHandSide(field);
// 				assignment.setOperator(Assignment.Operator.ASSIGN);
// 				assignment.setRightHandSide(ast.newSimpleName(fieldName));
// 				setterBody.statements().add(ast.newExpressionStatement(assignment));
// 				setter.setBody(setterBody);
				
// 				parent.bodyDeclarations().add(setter);
				
// 				return false;
// 			}
// 			});

// 		try {
// 			TextEdit edit = cu.rewrite(document, null);
// 			edit.apply(document);

// 			ClassAsString.classAsString = document.get();

// 			System.out.println(document.get());
// 		} catch (Exception e) {
// 			e.printStackTrace();
// 		}
// 	}

// 	public static String capitalize(String str) {
// 		return str.substring(0, 1).toUpperCase() + str.substring(1);
// 	}
// }
