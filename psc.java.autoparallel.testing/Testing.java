package psc.java.autoparallel.testing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.corext.fix.CompilationUnitRewriteOperationsFix.CompilationUnitRewriteOperation;
import org.eclipse.jdt.internal.ui.fix.AbstractMultiFix;
import org.eclipse.jdt.ui.cleanup.CleanUpOptions;
import org.eclipse.jdt.ui.cleanup.ICleanUp;
import org.eclipse.jdt.ui.cleanup.ICleanUpFix;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import psc.java.autoparallel.handlers.CheckCondition;
import psc.java.autoparallel.handlers.GraphBuilder;
import psc.java.autoparallel.handlers.JavaParserHelper;
import psc.java.autoparallel.handlers.MethodGraph;


@SuppressWarnings("restriction")
public class Testing extends AbstractMultiFix implements ICleanUp  {
	private CleanUpOptions fOptions;
	private RefactoringStatus fStatus;

	@Override
	public boolean canFix(ICompilationUnit compilationUnit, IProblemLocation problem) {
		return fOptions.isEnabled("cleanup.graph_method");
	}

	@Override
	public RefactoringStatus checkPostConditions(IProgressMonitor monitor) throws CoreException {
		try {
			if (fStatus == null || fStatus.isOK()) {
				return new RefactoringStatus();
			} else {
				return fStatus;
			}
		} finally {
			fStatus= null;
		}
	}

	@Override
	public RefactoringStatus checkPreConditions(IJavaProject project, ICompilationUnit[] compilationUnits,
			IProgressMonitor monitor) throws CoreException {
		if (fOptions.isEnabled("cleanup.graph_method")) { //$NON-NLS-1$
			fStatus= new RefactoringStatus();

//			ICompilationUnit[] files = findAllFilesProject(project);

			List<CompilationUnit> parsedCu = JavaParserHelper.parseSources(project, compilationUnits,monitor);

			MethodGraph graph = GraphBuilder.collectGraph(parsedCu);

			//exportGraph(graph);
			try {
				graph.exportDot(project.getProject().getLocation().toFile().getCanonicalPath() + "/graph.dot");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			/*
			 * Print method that parallelisable, modifLocal, readonly, ThreadSafe
			 */
			
			//print CheckCondition: is parrallelizable or not.
			for (CompilationUnit cu : parsedCu) {
				CheckCondition checkcon = new CheckCondition();
				cu.accept(checkcon);
				System.out.println("List of parallelizable method : " + checkcon.IsPara);
				//System.out.println("List of read only method : " + checkcon.ReadOnlyList);
				//System.out.println("List of modifLocal method : " + checkcon.ModifLocalList);
				//System.out.println("List of threadSafe method : " + checkcon.ThreadSafeList);
			}
			
			
			
		}
		return new RefactoringStatus();
	}
	@Override
	protected ICleanUpFix createFix(CompilationUnit unit, IProblemLocation[] problems) throws CoreException {
		return null;
	}
	@Override
	protected ICleanUpFix createFix(CompilationUnit cu) throws CoreException {
		if(cu == null || !fOptions.isEnabled("cleanup.graph_method")) {return null;}
		@SuppressWarnings("restriction")
		List<CompilationUnitRewriteOperation> rewriteOperations = new ArrayList<>();
		if(rewriteOperations.isEmpty())return null;
		return null;
	}

	@Override
	public void setOptions(CleanUpOptions options) {
		Assert.isLegal(options != null);
		Assert.isTrue(fOptions == null);
		fOptions= options;
	}
}
