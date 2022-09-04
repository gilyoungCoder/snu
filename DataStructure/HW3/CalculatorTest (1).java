import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;




public class CalculatorTest
{
	public static void main(String args[])
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		while (true)
		{
			try
			{
				String input = br.readLine();
				if (input.compareTo("q") == 0)
					break;

				command(input);
			}
			catch (Exception e)
			{
				System.out.println("ERROR");
			}
		}
	}

	 
	private static void command(String input)
	{
		toPostfix(input);
		
		long resultvalue = calculate();
		
		if (error==1) { 	//에러처리
			System.out.println("ERROR");
			error=0;
			postlist.clear();
		}
		
		else {
			doprint();
			System.out.println(resultvalue);
			postlist.clear();
			
		}

		
	}

	static Stack <String> oper = new Stack <>();
	static ArrayList<String> postlist = new ArrayList<>();
	static Stack <Long> calculate = new Stack <>();
	static String opset = "(^-*/%+";
	
	static int error = 0;
	
	private static long calculate() {	//스택 계산
		if (error==1) {return 0;}
		for (String j : postlist) {
			if (error==1) {break;}
			if (j.matches("^[0-9]+$")) {
				Long i = Long.parseLong(j);
				calculate.push(i);
			}
			
			else if(j.contains("~")){
				try {
					long a = -calculate.pop();
					calculate.push(a);	
				} 
				
				catch (Exception e) {
					makeError();
					break;
				}
				
				
			}

			else {
				long a = calculate.pop();
				long b = calculate.pop();
				long re = oneOperation(j,a,b);
				calculate.push(re);
			}
		}

		if (error==1) {return 0;}
		long result = calculate.pop();
		return result;
	}
	
	
	private static void checkParenthesis(String str) { 	//괄호 입력 오류 체크
		int count1 = 0;
		int count2 = 0;
		
		for (int i = 0; i<str.length(); i++) {
			if(str.substring(i,i+1).contains("(")) {
				count1+=1;
			}
			if(str.substring(i,i+1).contains(")")) {
				count2+=1;
			}
			if(count1 < count2) {
				makeError();
			}
		}
		
		if(count1!=count2) {
			makeError();
		}
	}
	
	private static String checkIntform(String input) {		//공백처리
		input = input.replaceAll("\t", " ");
		
		for(int i=0; i<input.length(); i++) {
			try {
				if(Character.isDigit(input.charAt(i))&&input.substring(i+1,i+2).contains(" ")){
					while(input.substring(i+2,i+3).contains(" ")) {
						i++;
					}
					i = i+2;
					if(Character.isDigit(input.charAt(i))) {
						error = 1;
						return "1";
					}
				}
			}
			
			catch (Exception e){
				continue;
			}			

		}
		
		input = input.replaceAll(" ", "");		
		return input;
	}
		
	
	private static void toPostfix(String input) //후위표현 변환
	{	
		
		input = checkIntform(input); 				
		String integer = "";
		checkParenthesis(input);
		
		for(int i=0; i<input.length(); i++) { 
			String cur = input.substring(i,i+1);
			char t = cur.charAt(0);
			if (error==1) {break;}
			if(Character.isDigit(t)) {			
				integer += cur;
				if (i==input.length()-1) {
					postlist.add(integer);
					integer="";
				}
				else {
					if(!Character.isDigit(input.charAt(i+1))) {
						postlist.add(integer);
						integer="";
					}
				}
			}
			
			
			else {
				
				if(opfilter(cur)) {
					break;
				}
				
				opTopost(cur, i, input);
			}
			
		}
		
		while(!oper.isEmpty()) {
			postlist.add(oper.pop());
		}

	}
	
	public static void doprint() {
		String postfix = String.join(" ", postlist);
		System.out.println(postfix);
	}
	
	private static boolean twoBinaryop(int i, String input) { //연산 입력 오류 처리
		if(i==0) {
			makeError();
			return true;
		}

		else {
			if(opset.contains(input.substring(i-1,i))) {
				makeError();
				return true;
			}
		}
		return false;
	}
	
	private static void opTopost(String cur, int i, String input) {		//후위 표현식 변환
		switch (cur) {
        
		case "(":
        	oper.push(cur);
        	break;
        
        case ")":
        	if(twoBinaryop(i, input)) {break;}
        	while(!"(".contains(oper.peek())) {
        		postlist.add(oper.pop());
        	}
        	oper.pop();
        	break;
	        	
        case "^":
        	if(twoBinaryop(i, input)) {break;}
        	if(i==input.length()-1) {
    			makeError();
    			break;
    		}
        	oper.push(cur);
        	break;
            	
        case "-":
            if(i==0) {
            	oper.push("~");
				break;
            }
            
            if(i==input.length()-1) {
            	makeError();
            	break;
            }
            
            else if(opset.contains(input.substring(i-1,i))) {
            	if(!oper.isEmpty()){
            		while(oper.peek()=="^") {
            			postlist.add(oper.pop());
            			if(oper.isEmpty()){ break;}
            		}
            	}
            	oper.push("~");
				break;
			}
        
            else {
            	if(!oper.isEmpty()){	
            		String str = "^*/%+-~";
            		
            		while(str.contains(oper.peek())) {
            			postlist.add(oper.pop());
                		if(oper.isEmpty()){ break;}
                	}
            	}
            	
            	oper.push(cur);
                 
            }
            
            break;
            
        case "*": case "/": case "%":
        	if(twoBinaryop(i, input)) {break;}
        	if(i==input.length()-1) {
    			makeError();
    			break;
    		}
        	
            else {
        	
            	 if(!oper.isEmpty()){
            		 String str = "^*/%~";
            		 while(str.contains(oper.peek())) {
            			 postlist.add(oper.pop());
            			 if(oper.isEmpty()){ break;}
            		 }
            	 }
            	 oper.push(cur);
             }
        	break;
        
        case "+":
        	if(twoBinaryop(i, input)) {break;}
        	if(i==input.length()-1) {
    			makeError();
    			break;
    		}
            else {
            	if(!oper.isEmpty()){
            		String str = "^*/%+-~";
            		while(str.contains(oper.peek())) {
            			postlist.add(oper.pop());
            			if(oper.isEmpty()){ break;}
            		}
            	 }
            	 oper.push(cur);
            }
            break;	
        
		}	
	}
	
	
	
	private static long oneOperation(String op, long a, long b) {		//연산
		switch (op) {
        
		case "^":
			if(b==0&&a<0) {
				makeError();
				return 0;
			}
			return (long)Math.pow(b, a);			
            	
            
        case "*": 
        	return b*a;
        
        case "/": 
        	if(a==0) {
				makeError();
				return 0;
			}
        
        	return b/a;
        
        case "%":
        	if(a==0) {
				makeError();
				return 0;
			}
        	
        	return b%a;
        	
        case "+":
        	return b+a;
        	
        case "-":
        	return b-a;
        
        default:
        	return 0;
        
		}	
	}


	
	public static boolean opfilter(String op) {	
		
		String[] infop = {"(", ")", "^", "*", "/", "%", "+", "-"};
		boolean t =false;
		
			if (!Arrays.asList(infop).contains(op)) {
				t = true;
				makeError();
			}
			
		return  t;
	}

	
	public static void makeError() {
		error=1;
	}
		


}

