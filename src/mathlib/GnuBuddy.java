package mathlib;
import qsymbol2.OutputFile;

public class GnuBuddy {

    public static void main(String[] s) {
	if (s.length == 1) {
	    OutputFile of = new OutputFile(s[0]+"-fid.gnu");
	    of.print("#automatically generated by GnuBuddy\n");
	    of.print("set output '"+s[0]+"-fid.ps'\n");
	    of.print("set terminal postscript\n");

	    for (int j = 0; j < 5; j++) {
		for (int i = 0; i < 4; i++) {
			// TODO made a fix here, might have broken, I don't know what was the original purpose
		    of.print("load '"+s[1]+(j*5+i)+".fid"+"'\n");
		}
	    }
	    of.close();
	    System.out.println("file '" + s[0]+"-fid.gnu' generated!");

	    of = new OutputFile(s[0]+"-prob.gnu");
	    of.print("#automatically generated by GnuBuddy\n");
	    of.print("set output '"+s[0]+"-prob.ps'\n");
	    of.print("set terminal postscript\n");

	    for (int j = 0; j < 5; j++) {
		for (int i = 0; i < 4; i++) {
			// TODO made a fix here, might have broken, I don't know what was the original purpose
		    of.print("load '"+s[1]+(j*5+i)+".prob"+"'\n");
		}
	    }
	    of.close();
	    System.out.println("file '" + s[0]+"-prob.gnu' generated!");
	}
	else {
	    System.out.println("inputfile expected!");
	    System.exit(-1);
	}

    }
}
