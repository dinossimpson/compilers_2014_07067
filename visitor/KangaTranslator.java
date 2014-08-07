//
// Generated by JTB 1.3.2 DIT@UoA patched
//

package visitor;
import syntaxtree.*;
import java.util.*;
import java.io.BufferedWriter;

/**
 * Provides default methods which visit each node in the tree in depth-first
 * order.  Your visitors may extend this class.
 */
public class KangaTranslator implements GJVisitor<String, String> {
  HashMap<String, HashMap<String, ArrayList<String>>> procStats; 
  HashMap<String, HashMap<String, HashSet<String>>> interferenceMap;
  HashMap<String, HashMap<String, String>> registerMap;
  HashMap<String, Stack<String>> spillStack;
  HashMap<String, ArrayList<Instruction>> cfg;
  HashMap<String, Instruction> labelInstructions;
  Stack<HashMap<String, String>> frameStack;
  Stack<Integer> curTop;
  ArrayList<String> orderOfProcs;
  // Stack<ArrayList<String>> frameStack2;
  HashMap<String, Integer> stackLocations;
  HashSet<String> sRegs;
  HashSet<String> argRegs;
  ArrayList<String> argList;
  ArrayList<String> protectedSRegs; 
  boolean labelPending;
  BufferedWriter out;
  String code;
  String curProcedure;
  int iCounter;
  int stackTop;

  public KangaTranslator(HashMap<String, HashMap<String, ArrayList<String>>> procStats, HashMap<String, HashMap<String, String>> registerMap,
                         HashMap<String, Stack<String>> spillStack, HashMap<String, ArrayList<Instruction>> cfg, HashMap<String, HashMap<String, HashSet<String>>> interferenceMap, HashMap<String, Instruction> labelInstructions, ArrayList<String> orderOfProcs, BufferedWriter out){
    this.procStats = procStats;
    this.registerMap = registerMap;
    this.spillStack = spillStack;
    this.cfg = cfg;
    this.interferenceMap = interferenceMap;
    this.labelInstructions = labelInstructions;
    this.orderOfProcs = orderOfProcs;
    List<String> regs = Arrays.asList("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7");
    this.sRegs = new HashSet<String>(regs);
    regs = Arrays.asList("a0", "a1", "a2", "a3");
    this.argRegs = new HashSet<String>(regs);
    this.frameStack = new Stack<HashMap<String, String>>();
    this.curTop = new Stack<Integer>();
    this.out = out;
    this.code = "";
    this.iCounter = 0;
    this.stackLocations = new HashMap<String, Integer>();
  }

  public void emit(String code){
    this.code += code;
  }

  public int getSpillIndex(String temp){
    int index = 0;

    // for(String spilledTemp : frameStack.get(curProcedure)){
    //   if(spilledTemp.equals(temp)){
    //     return index;
    //   }
    //   index++;
    // }
    return -1;
  }

  public Instruction getInstructionByText(String text){
    for(Instruction i : cfg.get(curProcedure)){
      if(i.getInstruction().equals(text))
        return i;
    }
    return null;
  }

  public Instruction getInstructionById(int id){
    for(Instruction i : cfg.get(curProcedure)){
      System.out.println(i.getId());
      if(i.getId() == id)
        return i;
    }
    return null;
  }

  // function to get next a reg
  public String getNextArgReg(){
    if(argRegs.contains("a0"))
      return "a0";
    else if(argRegs.contains("a1"))
      return "a1";
    else if(argRegs.contains("a2"))
      return "a2";
    else if(argRegs.contains("a3"))
      return "a3";
    return null;
  }

  // function to get next s reg
  public String getNextSReg(){
    String minSReg = "s7";
    for(String sreg : sRegs){
      if(sreg.compareTo(minSReg) < 0)
        minSReg = sreg;
    }
    if(!minSReg.equals("s7")){
      sRegs.remove(minSReg);
      return minSReg;
    }
    else{
      if(minSReg.contains("s7")){
        sRegs.remove(minSReg);
        return minSReg;
      }
    }
      return null;
  }

  public void getNextStackTop(){
    int stackSize=0;
    for(Integer stackPlaces : curTop){
      stackSize+= stackPlaces;
    }
    stackTop = stackSize-1;
  }

  public int getNextStackPos(){
    stackTop++;
    return stackTop;
  }

  public ArrayList<String> findSRegs(String method){
    ArrayList<String> saveRegs = new ArrayList<String>();
    for(String temp : interferenceMap.get(curProcedure).keySet()){
      String reg = registerMap.get(curProcedure).get(temp);
      if(reg != null && reg.contains("s")){
        saveRegs.add(reg);
      }
    }
    return saveRegs;
  }

  public boolean searchTemps(String temp){
    for (String curVal : procStats.get(curProcedure).get("Temps")){
      if (curVal.equals(temp)){
        return true;
      }
    }
    return false;
  }

   //
   // Auto class visitors--probably don't need to be overridden.
   //
   public String visit(NodeList n, String argu) {
      if (n.size() == 1)
         return n.elementAt(0).accept(this,argu);
      String _ret=null;
      int _count=0;
      for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
         e.nextElement().accept(this,argu);
         _count++;
      }
      return _ret;
   }

   public String visit(NodeListOptional n, String argu) {
      if ( n.present() ) {
         if (n.size() == 1)
            return n.elementAt(0).accept(this,argu);
         String _ret=null;
         int _count=0;
         for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
            e.nextElement().accept(this,argu);
            _count++;
         }
         return _ret;
      }
      else
         return null;
   }

   public String visit(NodeOptional n, String argu) {
      if ( n.present() )
         return n.node.accept(this,argu);
      else
         return null;
   }

   public String visit(NodeSequence n, String argu) {
      if (n.size() == 1)
         return n.elementAt(0).accept(this,argu);
      String _ret=null;
      int _count=0;
      for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
         e.nextElement().accept(this,argu);
         _count++;
      }
      return _ret;
   }

   public String visit(NodeToken n, String argu) { return null; }

   //
   // User-generated visitor methods below
   //

   /**
    * f0 -> "MAIN"
    * f1 -> StmtList()
    * f2 -> "END"
    * f3 -> ( Procedure() )*
    * f4 -> <EOF>
    */
   public String visit(Goal n, String argu) {
      String _ret=null;

      // for(String proc : orderOfProcs){
      //   System.out.println("in cfg's proc: "+proc);
      //   for(Instruction i : cfg.get(proc)){
      //     System.out.println(i.getInstruction());

      //   }
      //   try{System.in.read();}
      //   catch(Exception e){}
      // }

      for(String method : cfg.keySet()){
        Integer curCallStackLocs = new Integer(0);
        Integer stackLoc = new Integer(0);
        for(Instruction inst : cfg.get(method)){
          if(inst.getInstruction().contains("CALL")){
            for(String liveTTemp : inst.getOut()){
              String reg = registerMap.get(method).get(liveTTemp);
              System.out.println(reg+" = reg");
              if(reg.contains("t")){
                curCallStackLocs++;
              }
            }
            if(stackLoc < curCallStackLocs){
              System.out.println("found a bigger call");
              stackLoc = curCallStackLocs;
            }
            curCallStackLocs = 0;
          }
        }
        stackLocations.put(method, stackLoc);
      }
      System.out.println(stackLocations);
      try{System.in.read();}
      catch(Exception e){}

      curProcedure = "MAIN";
      n.f0.accept(this, argu);
      emit("MAIN\t[0]["+(stackLocations.get("MAIN")+1)+"]["+procStats.get(curProcedure).get("tempArgs").size()+"]\n");
      n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      emit("END\n");
      n.f3.accept(this, argu);
      n.f4.accept(this, argu);

      try {
        System.out.println("Final Codes:\n"+code);
        out.write(code);
        out.close();
      } catch( Exception ex ) {
        System.out.println( "Could not write to output file" );
      }
      System.out.println("Program translated successfully.");

      return _ret;
   }

   /**
    * f0 -> ( ( Label() )? Stmt() )*
    */
   public String visit(StmtList n, String argu) {
      labelPending = true;
      return n.f0.accept(this, argu);
   }

   /**
    * f0 -> Label()
    * f1 -> "["
    * f2 -> IntegerLiteral()
    * f3 -> "]"
    * f4 -> StmtExp()
    */
   public String visit(Procedure n, String argu) {
      String _ret=null;
      String name = n.f0.accept(this, argu);
      curProcedure = name;
      System.out.println("-------------------------in Procedure: "+name);
      System.out.println("with registerMap: "+registerMap.get(name));

      Integer stackPlaces = new Integer(0);
      frameStack.push(registerMap.get(name));
      for(String passargTemp : registerMap.get(name).keySet()){
        System.out.println(registerMap.get(name).get(passargTemp));
        if(registerMap.get(name).get(passargTemp).contains("PASSARG")){
          stackPlaces+=1;
        }
      }
      curTop.push(stackPlaces);
      getNextStackTop();

      int startingPoint;
      String sReg;
      int i;
      protectedSRegs = new ArrayList<String>();

      n.f1.accept(this, argu);
      String argNo = n.f2.accept(this, argu);
      n.f3.accept(this, argu);
      int secondBracket = 0;
      secondBracket = Integer.parseInt(procStats.get(curProcedure).get("argsNo").get(0)) - 4 + spillStack.get(curProcedure).size();

      // calculates the second brack in every procedure
      if(secondBracket<Integer.parseInt(procStats.get(curProcedure).get("argsNo").get(0)))
        secondBracket = Integer.parseInt(procStats.get(curProcedure).get("argsNo").get(0)) + stackLocations.get(curProcedure);
      emit(name+"\t["+argNo+"]["+secondBracket+"]["+procStats.get(curProcedure).get("tempArgs").size()+"]\n");

      // protects s regs that are going to be overwritten
      for( i=0; i<Integer.parseInt(procStats.get(curProcedure).get("argsNo").get(0)); i++ ){
        int stackPos = getNextStackPos();
        emit("\tASTORE SPILLEDARG "+stackPos+" s"+i+"\n");
        protectedSRegs.add("SPILLEDARG "+stackPos);
        // frameStack.push("s"+i);
        if(i==3)
          break;
      }

      // frameStack.clear();

      // saves arguments to s regs
      for( i=0; i<Integer.parseInt(procStats.get(curProcedure).get("argsNo").get(0)); i++ ){
        emit("\tMOVE s"+i+" a"+i+"\n");
        if(i==3)
          break;
      }      
      
      n.f4.accept(this, argu);
      
      int upperBound = Integer.parseInt(procStats.get(curProcedure).get("argsNo").get(0))-1;
      int j = 0;
      for(String spilledArg : protectedSRegs){
        emit("\tALOAD s"+j+" "+spilledArg+"\n");
        j++;
      }

      emit("END\n");
      frameStack.pop();
      curTop.pop();
      return _ret;
   }

   /**
    * f0 -> NoOpStmt()
    *       | ErrorStmt()
    *       | CJumpStmt()
    *       | JumpStmt()
    *       | HStoreStmt()
    *       | HLoadStmt()
    *       | MoveStmt()
    *       | PrintStmt()
    */
   public String visit(Stmt n, String argu) {
      labelPending = false;
      return n.f0.accept(this, argu);
   }

   /**
    * f0 -> "NOOP"
    */
   public String visit(NoOpStmt n, String argu) {
      boolean labelFound = false;
      for(String label : labelInstructions.keySet()){
        if(labelInstructions.get(label).getId() == iCounter){
          emit(label+"\tNOOP\n");
          labelFound = true;
          break;
        }
      }
      iCounter++;
      if(!labelFound)
        emit("\tNOOP\n");
      return n.f0.accept(this, argu);
   }

   /**
    * f0 -> "ERROR"
    */
   public String visit(ErrorStmt n, String argu) {
      iCounter++;
      emit("\tERROR\n");
      return n.f0.accept(this, argu);
   }

   /**
    * f0 -> "CJUMP"
    * f1 -> Temp()
    * f2 -> Label()
    */
   public String visit(CJumpStmt n, String argu) {
      String _ret=null;
      n.f0.accept(this, argu);
      String temp = n.f1.accept(this, argu);
      String label = n.f2.accept(this, argu);
      iCounter++;
      emit("\tCJUMP "+temp+" "+label+"\n");
      return _ret;
   }

   /**
    * f0 -> "JUMP"
    * f1 -> Label()
    */
   public String visit(JumpStmt n, String argu) {
      String _ret=null;
      n.f0.accept(this, argu);
      String label = n.f1.accept(this, argu);
      iCounter++;
      emit("\tJUMP "+label+"\n");
      return _ret;
   }

   /**
    * f0 -> "HSTORE"
    * f1 -> Temp()
    * f2 -> IntegerLiteral()
    * f3 -> Temp()
    */
   public String visit(HStoreStmt n, String argu) {
      String _ret=null;
      n.f0.accept(this, argu);
      emit("\tHSTORE ");
      String temp = n.f1.accept(this, argu);
      String intLit = n.f2.accept(this, argu);
      String temp2 = n.f3.accept(this, argu);
      iCounter++;
      emit(temp+" "+intLit+" "+temp2+"\n");
      return _ret;
   }

   /**
    * f0 -> "HLOAD"
    * f1 -> Temp()
    * f2 -> Temp()
    * f3 -> IntegerLiteral()
    */
   public String visit(HLoadStmt n, String argu) {
      String _ret=null;
      n.f0.accept(this, argu);
      String temp = n.f1.accept(this, argu);
      String temp2 = n.f2.accept(this, argu);
      String intLit = n.f3.accept(this, argu);
      iCounter++;
      emit("\tHLOAD "+temp+" "+temp2+" "+intLit+"\n");
      return _ret;
   }

   /**
    * f0 -> "MOVE"
    * f1 -> Temp()
    * f2 -> Exp()
    */
   public String visit(MoveStmt n, String argu) {
      String _ret=null;
      n.f0.accept(this, argu);
      String temp = n.f1.accept(this, argu);
      
      String exp = n.f2.accept(this, argu);

      if(exp.contains("ALOAD")){
        emit(exp);
        exp = "v1";
      }
      iCounter++;
      emit("\tMOVE "+temp+" "+exp+"\n");
      return _ret;
   }

   /**
    * f0 -> "PRINT"
    * f1 -> SimpleExp()
    */
   public String visit(PrintStmt n, String argu) {
      String _ret=null;
      n.f0.accept(this, argu);
      
      String simpleExp = n.f1.accept(this, argu);
      if(simpleExp.contains("ALOAD")){
        emit(simpleExp);
        emit("\tPRINT v1\n");
      }
      else{
        emit("\tPRINT "+simpleExp+"\n");
      }
      iCounter++;
      
      return _ret;
   }

   /**
    * f0 -> Call()
    *       | HAllocate()
    *       | BinOp()
    *       | SimpleExp()
    */
   public String visit(Exp n, String argu) {
      return n.f0.accept(this, argu);
   }

   /**
    * f0 -> "BEGIN"
    * f1 -> StmtList()
    * f2 -> "RETURN"
    * f3 -> SimpleExp()
    * f4 -> "END"
    */
   public String visit(StmtExp n, String argu) {
      String _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      String simpleExp = n.f3.accept(this, argu);
      if(!curProcedure.equals("MAIN"))
        emit("\tMOVE v0 "+simpleExp+"\n");
      n.f4.accept(this, argu);
      iCounter++;
      return _ret;
   }

   /**
    * f0 -> "CALL"
    * f1 -> SimpleExp()
    * f2 -> "("
    * f3 -> ( Temp() )*
    * f4 -> ")"
    */
   public String visit(Call n, String argu) {
      String _ret=null;
      // Instruction instruction = getInstructionById(iCounter);
      HashSet<String> callOut = new HashSet<String>();


      n.f0.accept(this, argu);
      argList = new ArrayList<String>();
      int i = 0;
      // System.out.println(curProcedure+"'s arguments: "+procStats.get(curProcedure).get("tempArgs"));

      String simpleExp = n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      n.f3.accept(this, "arg");

      i=0;
      // System.out.println(instruction.getInstruction()+" has arguments: "+argList );
      System.out.println(argList);
      for(String arg : argList){
        String reg = registerMap.get(curProcedure).get(arg);

        System.out.println("argument: "+arg+" gets assigned to: "+reg);
        System.out.println(i);
        // try{System.in.read();}
        // catch(Exception e){}

        if(reg == null){
          emit("ALOAD v1 SPILLEDARG "+getSpillIndex(arg));
          reg = "v1";
        }
        else if(reg.contains("PASSARG")){
          int spilledArgNo = Integer.parseInt(reg.substring(reg.length()-1, reg.length()));
          emit("\tALOAD v1 SPILLEDARG "+(spilledArgNo-1)+"\n");
          reg = "v1";
        }
        if(i < 4)
          emit("\tMOVE a"+i+" "+reg+"\n");
        else{
          emit("\tPASSARG "+(i-3)+" "+reg+"\n");
          // frameStack.push(reg);
        }
        i++;
      }
      System.out.println(iCounter);
      // Instruction instruction = getInstructionByText("CALL "+simpleExp+"\n");
      Instruction instruction = getInstructionById(iCounter);
      System.out.println(instruction);
      callOut = instruction.getOut();
      // Stack<String> tempStack = new Stack<String>();
      
      int numberOfSaves = 0;
      int savedNumber = 0;
      for(String liveTTemp : callOut){
        if(registerMap.get(curProcedure).get(liveTTemp).contains("t")){
          System.out.println(registerMap.get(curProcedure).get(liveTTemp)+" needs to be saved");
          emit("\tASTORE SPILLEDARG "+getNextStackPos()+" "+registerMap.get(curProcedure).get(liveTTemp)+"\n");
          numberOfSaves++;
        }
      }
      savedNumber = numberOfSaves;
        // System.out.println(callOut);
        // try{System.in.read();}
        // catch(Exception e){}
      emit("\tCALL "+simpleExp+"\n");
      

      for(String liveTTemp : callOut){
        if(registerMap.get(curProcedure).get(liveTTemp).contains("t")){
          emit("\tALOAD v1 SPILLEDARG "+(stackTop-numberOfSaves+1)+"\n");
          emit("\tMOVE "+registerMap.get(curProcedure).get(liveTTemp)+" v1"+"\n");
          numberOfSaves--;
        }
      }
      stackTop -= savedNumber;
      n.f4.accept(this, argu);
      return "v0";
   }

   /**
    * f0 -> "HALLOCATE"
    * f1 -> SimpleExp()
    */
   public String visit(HAllocate n, String argu) {
      String _ret=null;
      n.f0.accept(this, argu);
      // emit("\tHALLOCATE ");
      String simpleExp = n.f1.accept(this, argu);
      // emit(simpleExp+"\n");
      return "HALLOCATE "+simpleExp;
   }

   /**
    * f0 -> Operator()
    * f1 -> Temp()
    * f2 -> SimpleExp()
    */
   public String visit(BinOp n, String argu) {
      String _ret=null;
      String operator = n.f0.accept(this, argu);
      String temp = n.f1.accept(this, argu);
      String simpleExp = n.f2.accept(this, argu);
      return operator+" "+temp+" "+simpleExp;
   }

   /**
    * f0 -> "LT"
    *       | "PLUS"
    *       | "MINUS"
    *       | "TIMES"
    */
   public String visit(Operator n, String argu) {
      n.f0.accept(this, argu);
      return n.f0.choice.toString();
   }

   /**
    * f0 -> Temp()
    *       | IntegerLiteral()
    *       | Label()
    */
   public String visit(SimpleExp n, String argu) {
      return n.f0.accept(this, argu);
   }

   /**
    * f0 -> "TEMP"
    * f1 -> IntegerLiteral()
    */
   public String visit(Temp n, String argu) {
      String _ret=null;
      n.f0.accept(this, argu);
      String tempNo = n.f1.accept(this, argu);
      String kangaTemp = registerMap.get(curProcedure).get("TEMP "+tempNo);
      if(argu.equals("arg")){
        argList.add("TEMP "+tempNo);
      }
      if(kangaTemp == null){
        if(spillStack.get(curProcedure).contains("TEMP "+tempNo)){
          return "ALOAD v1 SPILLEDARG "+getSpillIndex("TEMP "+tempNo);
        }
        return "s7";
      }
      else if(kangaTemp.contains("PASSARG")){
        int spilledArg = Integer.parseInt(kangaTemp.substring(kangaTemp.length()-1, kangaTemp.length()));        
        return "\tALOAD v1 SPILLEDARG "+(spilledArg-1)+"\n";
      }
      return kangaTemp;
   }

   /**
    * f0 -> <INTEGER_LITERAL>
    */
   public String visit(IntegerLiteral n, String argu) {
      n.f0.accept(this, argu);
      return n.f0.toString();
   }

   /**
    * f0 -> <IDENTIFIER>
    */
   public String visit(Label n, String argu) {
      n.f0.accept(this, argu);
      // if(labelPending)
        // emit(n.f0.toString());
      return n.f0.toString();
   }

}
