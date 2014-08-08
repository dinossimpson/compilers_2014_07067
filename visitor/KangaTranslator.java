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
  HashMap<String, Integer> stackLocations;
  HashMap<String, Stack<String>> methodStackMap;
  HashSet<String> sRegs;
  HashSet<String> argRegs;
  ArrayList<String> argList;
  ArrayList<String> protectedSRegs; 
  BufferedWriter out;
  String code;
  String curProcedure;
  int iCounter;
  int stackTop;
  HashSet<Integer> deadCodeList;


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
    this.deadCodeList = new HashSet<Integer>();
    this.methodStackMap = new HashMap<String, Stack<String>>();
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
      // // System.out.println(i.getId());
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
    Integer tempLastStackPlaces = curTop.pop();
    for(Integer stackPlaces : curTop){
      stackSize+= stackPlaces;
    }
    curTop.push(tempLastStackPlaces);
    stackTop = stackSize-1;
    if(Integer.parseInt(procStats.get(curProcedure).get("argsNo").get(0)) > 3)
      stackTop += Integer.parseInt(procStats.get(curProcedure).get("argsNo").get(0)) - 4;
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

      HashMap<String, Integer> defedTemps = new HashMap<String, Integer>();
      for(String method : cfg.keySet()){
       
        for(Instruction instr : cfg.get(method)){
          for(String tempUse : instr.getUse()){
            defedTemps.remove(tempUse);  
          }
          if(instr.getType().equals("JUMP") || instr.getType().equals("CJUMP")){
            defedTemps.clear();
            continue;
          }
          if(!instr.getType().equals("MOVE") && !instr.getType().equals("HLOAD"))
            continue;
          if(defedTemps.containsKey(instr.getDef().iterator().next())){
            deadCodeList.add(defedTemps.get(instr.getDef().iterator().next()));
          }
          else{
            defedTemps.put(instr.getDef().iterator().next(), instr.getId());
          }
        }

        // System.out.println("REMAINING TEMPS NOT USED IN METHOD: "+defedTemps);
        Collection c = defedTemps.values();
        Iterator itr = c.iterator();
        while (itr.hasNext()) {
          deadCodeList.add((Integer)itr.next());
        }
      }
      
      /////////////////////////////////////////////////////////////////////////////////
      for(String method : cfg.keySet()){
        Integer curCallStackLocs = new Integer(0);
        Integer stackLoc = new Integer(0);
        HashSet<String> mainStackRegs = new HashSet<String>();

        for(Instruction inst : cfg.get(method)){
          if(inst.getInstruction().contains("CALL")){
            HashSet<String> intersection = new HashSet<String>(inst.getOut());
            intersection.retainAll(inst.getIn());
            for(String liveTTemp : intersection){
              String reg = registerMap.get(method).get(liveTTemp);
              if(reg.contains("t")){
                curCallStackLocs++;
              }
            }
            if(stackLoc < curCallStackLocs){
              stackLoc = curCallStackLocs;
            }
            curCallStackLocs = 0;
          }
        }
        for(String temp : registerMap.get(method).keySet()){
          if(registerMap.get(method).get(temp).contains("s") ||
             registerMap.get(method).get(temp).contains("SPILL") || 
             registerMap.get(method).get(temp).contains("PASSARG")){
            // stackLoc++;
            mainStackRegs.add(registerMap.get(method).get(temp));
          }
        }
        stackLocations.put(method, stackLoc+mainStackRegs.size());
      }

      /////////////////////////////////////////////////////////////////////////////////

      curProcedure = "MAIN";
      n.f0.accept(this, argu);
      emit("MAIN\t[0]["+stackLocations.get("MAIN")+"]["+procStats.get(curProcedure).get("tempArgs").size()+"]\n");
      n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      emit("END\n");
      n.f3.accept(this, argu);
      n.f4.accept(this, argu);

      try {
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
      Stack<String> methodStack = new Stack<String>();
      HashMap<String, String> sRegProtect = new HashMap<String, String>();

      String name = n.f0.accept(this, argu);
      curProcedure = name;
      // System.out.println("-------------------------in Procedure: "+name+" "+Integer.parseInt(procStats.get(curProcedure).get("argsNo").get(0)));
      // System.out.println("with registerMap: "+registerMap.get(name));
      // System.out.println("with spillStack: "+spillStack.get(name));

      Integer stackPlaces = new Integer(0);
      frameStack.push(registerMap.get(name));

      for(String passargTemp : registerMap.get(name).keySet()){
        if((registerMap.get(name).get(passargTemp).contains("PASSARG") || 
            registerMap.get(name).get(passargTemp).contains("s") || 
            registerMap.get(name).get(passargTemp).contains("SPILL"))
            && !methodStack.contains(registerMap.get(name).get(passargTemp))){
          methodStack.push(registerMap.get(name).get(passargTemp));
        }
      }
      methodStackMap.put(curProcedure, methodStack);
      
      curTop.push(methodStack.size());
      getNextStackTop();

      n.f1.accept(this, argu);
      String argNo = n.f2.accept(this, argu);
      n.f3.accept(this, argu);
      int secondBracket = stackLocations.get(curProcedure);

      System.out.println("secondBracket in "+name+" = "+secondBracket);
      emit(name+"\t["+argNo+"]["+secondBracket+"]["+procStats.get(curProcedure).get("tempArgs").size()+"]\n");

      for( String temp : registerMap.get(curProcedure).keySet()){
        String reg = registerMap.get(curProcedure).get(temp);
        if(reg.contains("s") && !sRegProtect.containsKey(reg)){
          int stackPos = getNextStackPos();
          emit("\tASTORE SPILLEDARG "+stackPos+" "+reg+"\n");
          sRegProtect.put(reg, "SPILLEDARG "+stackPos);
        }
      }
      
      // saves arguments to s regs
      for( int i=0; i<Integer.parseInt(procStats.get(curProcedure).get("argsNo").get(0)); i++ ){
        emit("\tMOVE s"+i+" a"+i+"\n");
        if(i==3)
          break;
      }      
      
      n.f4.accept(this, argu);

      for(String savedReg : sRegProtect.keySet()){
        emit("\tALOAD "+savedReg+" "+sRegProtect.get(savedReg)+"\n");
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
      Instruction i;
      n.f0.accept(this, argu);
      String temp = n.f1.accept(this, argu);
      String temp2 = n.f2.accept(this, argu);
      String intLit = n.f3.accept(this, argu);
      
      if(deadCodeList.contains(iCounter)){
        //// System.out.println("FOUND DEAD!: "+getInstructionById(iCounter).getInstruction());
        emit("NOOP\n");
      }
      else
        emit("\tHLOAD "+temp+" "+temp2+" "+intLit+"\n");
      iCounter++;
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
      
      if(deadCodeList.contains(iCounter)){
        //// System.out.println("FOUND DEAD!: "+getInstructionById(iCounter).getInstruction());
        emit("NOOP\n");
      }
      else
        emit("\tMOVE "+temp+" "+exp+"\n");
      iCounter++;

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
      HashSet<String> callIn = new HashSet<String>();


      n.f0.accept(this, argu);
      argList = new ArrayList<String>();
      int i = 0;

      String simpleExp = n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      n.f3.accept(this, "arg");

      i=0;

      for(String arg : argList){
        String reg = registerMap.get(curProcedure).get(arg);

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
        }
        i++;
      }
      Instruction instruction = getInstructionById(iCounter);
      callOut = instruction.getOut();
      callIn = instruction.getIn();
      HashSet<String> intersection = new HashSet<String>(callOut);
      intersection.retainAll(callIn);
      
      int numberOfSaves = 0;
      int savedNumber = 0;
      for(String liveTTemp : intersection){
        if(registerMap.get(curProcedure).get(liveTTemp).contains("t")){
          emit("\tASTORE SPILLEDARG "+getNextStackPos()+" "+registerMap.get(curProcedure).get(liveTTemp)+"\n");
          numberOfSaves++;
        }
      }
      savedNumber = numberOfSaves;
      emit("\tCALL "+simpleExp+"\n");

      for(String liveTTemp : intersection){
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
      String simpleExp = n.f1.accept(this, argu);
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
          System.out.println("actually spilled one");
          return "ALOAD v1 SPILLEDARG "+getSpillIndex("TEMP "+tempNo);
        }
        return "v0";
      }
      else if(kangaTemp.contains("PASSARG")){
        int spilledArg = methodStackMap.get(curProcedure).search(kangaTemp);
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
      return n.f0.toString();
   }

}
