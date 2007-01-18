/*
 * Created on May 16, 2006
 */
package com.python.pydev.analysis.scopeanalysis;

import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.Document;
import org.python.pydev.core.IToken;
import org.python.pydev.core.docutils.PySelection;
import org.python.pydev.editor.codecompletion.revisited.modules.AbstractModule;
import org.python.pydev.editor.codecompletion.revisited.modules.SourceModule;
import org.python.pydev.editor.codecompletion.revisited.modules.SourceToken;
import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.visitors.NodeUtils;

import com.python.pydev.analysis.AnalysisTestsBase;
import com.python.pydev.analysis.messages.AbstractMessage;

public class ScopeAnalyzerVisitorTest extends AnalysisTestsBase {

    public static void main(String[] args) {
    	try {
			ScopeAnalyzerVisitorTest test = new ScopeAnalyzerVisitorTest();
			test.setUp();
			test.testIt24();
			test.tearDown();
			junit.textui.TestRunner.run(ScopeAnalyzerVisitorTest.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    private Document doc;

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testIt2() throws Exception {
    	doc = new Document(
    			"import os\n"+
    			"print os\n"+
    			"\n"
    	);
    	int line=0;
    	int col=8;
    	List<IToken> tokenOccurrences = getTokenOccurrences(line, col);
    	assertEquals(2, tokenOccurrences.size());
    	
    	assertEquals(0, AbstractMessage.getStartLine(tokenOccurrences.get(0), doc)-1);
    	assertEquals(7, AbstractMessage.getStartCol(tokenOccurrences.get(0), doc)-1);
    	
    	assertEquals(1, tokenOccurrences.get(1).getLineDefinition()-1);
    	assertEquals(6, tokenOccurrences.get(1).getColDefinition()-1);
    }    	
    
    public void testIt4() throws Exception {
    	doc = new Document(
    			"print os\n"+
    			"\n"
    	);
    	int line=0;
    	int col=7;
    	//if we don't have the definition, we may still have occurrences
    	List<IToken> tokenOccurrences = getTokenOccurrences(line, col);
    	assertEquals(1, tokenOccurrences.size());
    }    	
    
    public void testIt5() throws Exception {
    	doc = new Document(
    			"foo = 10\n" +
    			"print foo\n" +
    			"foo = 20\n"+
    			"print foo\n" +
    			"\n"
    	);
    	int line=0;
    	int col=0;
    	//if we don't have the definition, we don't have any references...
    	List<IToken> tokenOccurrences = getTokenOccurrences(line, col);
    	assertEquals(4, tokenOccurrences.size());
    }    	
    
    
    public void testIt6() throws Exception {
    	doc = new Document(
    			"foo = 10\n" +
    			"foo.a = 10\n" +
    			"print foo.a\n" +
    			"foo.a = 20\n"+
    			"print foo.a\n" +
    			"\n"
    	);
    	
    	assertEquals(4, getTokenOccurrences(1, 4).size());
    	assertEquals(5, getTokenOccurrences(1, 0).size());
    }    	
    
    
    public void testIt3() throws Exception {
    	doc = new Document(
    			"import os.path.os\n"+
    			"print  os.path.os\n"+
    			"\n"
    	);
    	checkTestResults(0, 10, "path");
    	checkTestResults(1, 10, "path");
    	
    	checkTestResults(0, 7, "os");
    	checkTestResults(1, 7, "os");
    	
    	checkTestResults(0, 15, "os");
    	checkTestResults(1, 15, "os"); //let's see if it checks the relative position correctly
    }
    
    public void testIt7() throws Exception {
    	doc = new Document(
    			"from os import path\n"+
    			"print          path\n"+
    			"\n"
    	);
    	checkTestResults(0, 15, "path");
    	checkTestResults(1, 15, "path");
    }
    
    
    public void testIt8() throws Exception {
    	doc = new Document(
    			"from os import foo, path\n"+
    			"print               path\n"+
    			"\n"
    	);
    	checkTestResults(0, 20, "path");
    	checkTestResults(1, 20, "path");
    }
    
    public void testIt9() throws Exception {
    	doc = new Document(
    			"from os import foo as path\n"+
    			"print                 path\n"+
    			"\n"
    	);
    	checkTestResults(0, 22, "path");
    	checkTestResults(1, 22, "path");
    }
    
    public void testIt10() throws Exception {
    	doc = new Document(
    			"import path as foo\n"+
    			"print          foo\n"+
    			"\n"
    	);
    	checkTestResults(0, 15, "foo");
    	checkTestResults(1, 15, "foo");
    }
    
    public void testIt11() throws Exception {
    	doc = new Document(
    			"print          foo\n"+
    			"print          foo\n"+
    			"\n"
    	);
    	checkTestResults(0, 15, "foo");
    	checkTestResults(1, 15, "foo", false);
    }
    
    public void testIt12() throws Exception {
    	doc = new Document(
    			"def m1():\n" +
    			"    print foo\n"+
    			"    print bla.foo\n"+
    			"\n"
    	);
    	List<IToken> tokenOccurrences = getTokenOccurrences(1, 11);
    	assertEquals(1, tokenOccurrences.size());
    }
    
    public void testIt13() throws Exception {
        doc = new Document(
                "def m1():\n" +
                "    print foo.bla\n" + //accessing this should not get the locals
                "    print bla\n"
        );
        List<IToken> tokenOccurrences = getTokenOccurrences(1, 15);
        assertEquals(1, tokenOccurrences.size());
        
    }
    
    public void testIt14() throws Exception {
    	doc = new Document(
    			"def checkProps(self):\n" +
    			"    getattr(self).value\n" +
    			"\n"
    	);
    	List<IToken> tokenOccurrences = getTokenOccurrences(0, 16);
    	assertEquals(2, tokenOccurrences.size());
    }
    
    public void testIt15() throws Exception {
    	doc = new Document(
    			"from testrec2.core import leaf\n" +
    			"class Foo(leaf.Leaf):\n" + //on the Leaf part
    			"    def setUp(self):\n" +
    			"        leaf.Leaf.setUp(self)"
    	);
    	List<IToken> tokenOccurrences = getTokenOccurrences(1, 17);
    	assertEquals(2, tokenOccurrences.size());
        assertEquals(1, tokenOccurrences.get(0).getLineDefinition()-1);
        assertEquals(15, tokenOccurrences.get(0).getColDefinition()-1);
        
        assertEquals(3, tokenOccurrences.get(1).getLineDefinition()-1);
        assertEquals(13, tokenOccurrences.get(1).getColDefinition()-1);
    }
    
    public void testIt16() throws Exception {
        doc = new Document(
                "import b    \n" +
                "print b.bar\n" + //this one is selected (b is undefined)
                "class C2:\n" +
                "    def m1(self):\n" +
                "        bar = 10\n" //should not get this one
        );
        List<IToken> tokenOccurrences = getTokenOccurrences(1, 10);
        assertEquals(1, tokenOccurrences.size());
    }
    
    public void testIt17() throws Exception {
        doc = new Document(
                "import testlib    \n" +
                "print testlib.bar\n" + //this one is selected (bar is undefined)
                "class C2:\n" +
                "    def m1(self):\n" +
                "        bar = 10\n" //should not get this one
        );
        List<IToken> tokenOccurrences = getTokenOccurrences(1, 16);
        assertEquals(1, tokenOccurrences.size());
    }
    
    public void testIt18() throws Exception {
    	doc = new Document(
    	    	"import bla as fooo\n" +
    	    	"raise fooo.ffff(msg)\n"
    	);
    	List<IToken> tokenOccurrences = getTokenOccurrences(0, 16);
    	assertEquals(2, tokenOccurrences.size());
    	
    	IToken t0 = tokenOccurrences.get(0);
    	IToken t1 = tokenOccurrences.get(1);
    	SimpleNode ast0 = ((SourceToken)t0).getAst();
    	SimpleNode ast1 = ((SourceToken)t1).getAst();
    	
		assertEquals("fooo", t0.getRepresentation());
		assertEquals("fooo", t1.getRepresentation());
    	assertEquals(15, NodeUtils.getColDefinition(ast0));
    	assertEquals(7, NodeUtils.getColDefinition(ast1));
    }
    
    
    public void testIt19() throws Exception {
        doc = new Document(
                "import os.path\n" +
                "print os.path\n"
        );
        List<IToken> tokenOccurrences = getTokenOccurrences(0, 10);
        assertEquals(2, tokenOccurrences.size());
        
    }
    
    public void testIt20() throws Exception {
        doc = new Document(
        "def m1():           \n" +
        "    class LocalFoo: \n" +
        "        pass        \n" +
        "    print LocalFoo  \n" +
        "                    \n" +
        "class LocalFoo:     \n" +
        "    pass            \n" +
        "print LocalFoo      \n" 
        );
        List<IToken> tokenOccurrences = getTokenOccurrences(1, 13);
        assertEquals(2, tokenOccurrences.size());
        
    }
    
    public void testIt21() throws Exception {
    	doc = new Document(
    			"class LocalFoo: \n" +
    			"    pass        \n" +
    			"print LocalFoo  \n"
    	);
    	List<IToken> tokenOccurrences = getTokenOccurrences(2, 8);
    	assertEquals(2, tokenOccurrences.size());
    	
    }
    
    public void testIt22() throws Exception {
        doc = new Document(
                "class LocalFoo(object): \n" +
                "    pass        \n"
        );
        List<IToken> tokenOccurrences = getTokenOccurrences(0, 17);
        assertEquals(1, tokenOccurrences.size());
    }
    
    public void testIt23() throws Exception {
    	doc = new Document(
    			"class Foo(object): \n" +
    			"    pass        \n"
    	);
    	List<IToken> tokenOccurrences = getTokenOccurrences(0, 7);
    	assertEquals(1, tokenOccurrences.size());
    }
    
    public void testIt24() throws Exception {
        doc = new Document(
                "import os\n" +
                "from os import path\n"
        );
        List<IToken> tokenOccurrences = getTokenOccurrences(0, 8);
        assertEquals(2, tokenOccurrences.size());
    }
    
//    do we want to check self ?
//    public void testIt16() throws Exception {
//    	doc = new Document(
//    			"class Foo:\n" +
//    			"    vlMolecularWeigth = ''\n" +
//    			"    def toSimulator(self):\n" +
//    			"        print self.vlMolecularWeigth\n" +
//    			""
//    	);
//    	List<IToken> tokenOccurrences = getTokenOccurrences(3, 21);
//    	assertEquals(2, tokenOccurrences.size());
//    }

    private void checkTestResults(int line, int col, String lookFor) throws Exception {
    	checkTestResults(line, col, lookFor, true);
    }
    
	private void checkTestResults(int line, int col, String lookFor, boolean checkPositions) throws Exception {
		List<IToken> tokenOccurrences = getTokenOccurrences(line, col);
    	assertEquals(2, tokenOccurrences.size());
    	
    	IToken tok0 = tokenOccurrences.get(0);
    	assertEquals(lookFor, tok0.getRepresentation());
    	if(checkPositions){
			assertEquals(0, AbstractMessage.getStartLine(tok0, doc)-1);
	    	assertEquals(col, AbstractMessage.getStartCol(tok0, doc)-1);
    	}
    	
    	IToken tok1 = tokenOccurrences.get(1);
    	assertEquals(lookFor, tok1.getRepresentation());
    	if(checkPositions){
			assertEquals(1, AbstractMessage.getStartLine(tok1, doc)-1);
	    	assertEquals(col, AbstractMessage.getStartCol(tok1, doc)-1);
    	}
	}    	
    
    
	
	public void testIt() throws Exception {
        doc = new Document(
                "foo = 20\n"+
                "print foo\n"+
                "\n"
        );
        
    	List<IToken> toks = getTokenOccurrences(0, 1);
    	assertEquals(2, toks.size());
    	assertEquals(0, toks.get(0).getLineDefinition()-1);
    	assertEquals(0, toks.get(0).getColDefinition()-1);
    	
    	assertEquals(1, toks.get(1).getLineDefinition()-1);
    	assertEquals(6, toks.get(1).getColDefinition()-1);
	}
	
	/**
	 * @param line: 0 based
	 * @param col: 0 based
	 */
	private List<IToken> getTokenOccurrences(int line, int col) throws Exception {
		ScopeAnalyzerVisitor visitor = doVisit(line, col);
		List<IToken> ret = visitor.getTokenOccurrences();
        return ret;
	}


	private ScopeAnalyzerVisitor doVisit(int line, int col) throws Exception {
		SourceModule mod = (SourceModule) AbstractModule.createModuleFromDoc(null, null, doc, nature, 0);
		PySelection ps = new PySelection(doc, line, col);
        ScopeAnalyzerVisitor visitor = new ScopeAnalyzerVisitor(nature, "mod1", mod, doc, new NullProgressMonitor(), ps);
        mod.getAst().accept(visitor);
		return visitor;
	}

}
