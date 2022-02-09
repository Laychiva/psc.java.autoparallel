package psc.java.autoparallel.handlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.SynchronizedStatement;

public class CheckCondition extends ASTVisitor {

	public List<MethodDeclaration> ReadOnlyList = new ArrayList<>(); //ReadOnly List
	public List<MethodDeclaration> ModifLocalList = new ArrayList<>();	//ModifLocal List
	public List<MethodDeclaration> IsPara = new ArrayList<>();	//Parallelizable list
	public List<MethodDeclaration> ThreadSafeList = new ArrayList<>();	// ThreadSafe List

	@Override
	public boolean visit(MethodDeclaration node) {
		boolean ThreadSafe = false;
		CheckConditions CheckCons = new CheckConditions();
		node.accept(CheckCons);
		if(!CheckCons.hasWrite) {
			ThreadSafe = true;
			ReadOnlyList.add(node); // add method to list
			ThreadSafeList.add(node);  // first case if it is readonly then it is threadsafe
		}
		if(CheckCons.isModifLocal && !CheckCons.hasWrite) { // if Method modifies the local variable and doesn't modifies the field 
															// then it is modifLocal
			ModifLocalList.add(node); // add method to list
		}
		/*
		 * If Method is ThreadSafe, ReadOnly then it is parallelisable
		 */
		if(!CheckCons.hasWrite && ThreadSafe == true) {
			IsPara.add(node);
		}
		return super.visit(node);
	}
}

class CheckConditions extends ASTVisitor {
	boolean hasWrite = false;
	boolean isModifLocal = false;
	boolean ThreadSafe = false;


	public boolean isThreadSafe() {
		return ThreadSafe;
	}
	
	
	@Override
	// Visit assignment operator
	public boolean visit(Assignment node) {	
		AssignmentVisitor av = new AssignmentVisitor();
		node.getLeftHandSide().accept(av);
		if (av.hasWrite) {
			hasWrite = true ; 
		}

		if (av.isModifLocal) {
			isModifLocal = true;	
		}

		return super.visit(node);
	}
	@Override
	//visit postfixexpression
	public boolean visit(PostfixExpression node) {
		AssignmentVisitor av = new AssignmentVisitor();
		node.getOperand().accept(av);
		if (av.hasWrite) {
			hasWrite = true;	
		}
		if (av.isModifLocal) {
			isModifLocal = true;	
		}
		return super.visit(node);
	}
	@Override
	//visit prefixexpression
	public boolean visit(PrefixExpression node) {
		AssignmentVisitor av = new AssignmentVisitor();
		node.getOperand().accept(av);
		if (av.hasWrite) {
			hasWrite = true;
		}
		if (av.isModifLocal) {
			isModifLocal = true;	
		}
		return super.visit(node);
	}
}
class AssignmentVisitor extends ASTVisitor {
	boolean hasWrite = false;
	boolean isModifLocal = false;
	@Override
	public void preVisit(ASTNode node) {
		if (node instanceof Name) { //simple name and qualifiedname
			Name nname = (Name) node;
			IBinding target = nname.resolveBinding();
			if (target instanceof IVariableBinding) {
				IVariableBinding vbind = (IVariableBinding) target;
				if (vbind.isField()) {	//	if the operation is assigned on a field then it is not readOnly
					hasWrite = true;
				}
				else if(!vbind.isField()) { //	if the operation isn't assigned on field then it is modifLocal
					isModifLocal = true;
				}
			}
		}
		super.preVisit(node);
	}
}
