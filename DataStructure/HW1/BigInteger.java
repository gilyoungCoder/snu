import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BigInteger {
	public static final String QUIT_COMMAND = "quit";
	public static final String MSG_INVALID_INPUT = "입력이 잘못됐습니다.";
	public static final Pattern EXPRESSION_PATTERN = Pattern.compile("\\s*([+\\-])?\\s*(\\d+)\\s*([+\\-*])\\s*([+\\-])?\\s*(\\d+)\\s*");
	// implement this

	public int[] num = new int[200];

	public int sign = 1;

	public int max(BigInteger b){ 
		int re = 1;
		for(int i = 0; i<100; i++) {
			if(this.num[99-i]>b.num[99-i]) {
				re=1;
				break;
			}
			else if(this.num[99-i]<b.num[99-i]) {
				re=0;
				break;
			}

		}

		return re;
	}



	public BigInteger(String s)
	{
		for(int i=1; i<=s.length(); i++ ) {
			this.num[s.length()-i]=Integer.parseInt(s.substring(i-1,i));
		}
	}


	public BigInteger add(BigInteger big)
	{
		BigInteger re = new BigInteger("");
		for(int i=0; i<100; i++) {
			int x = this.num[i] + big.num[i];
			if (x<10) {
				re.num[i] = x;
			}
			else {
				re.num[i]=x-10;
				if(i<99) {
					big.num[i+1]+=1;
				}
				else {
					re.num[i+1]=1;
				}
			}
		}
		return re;
	}

	public BigInteger subtract(BigInteger big)
	{
		BigInteger re = new BigInteger("");
		if(this.max(big)==1) {
			for(int i=0; i<100; i++) {
				int x = this.num[i] - big.num[i];
				if (x>=0) {
					re.num[i] = x;
				}
				else {
					re.num[i]=x+10;
					if(i<99) {
						this.num[i+1]-=1;
					}
					else {
						;
					}
				}
			}
		}
		else {
			for(int i=0; i<100; i++) {
				int x = -this.num[i] + big.num[i];
				if (x>=0) {
					re.num[i] = x;
				}
				else {
					re.num[i]=x+10;
					if(i<99) {
						big.num[i+1]-=1;
					}
					else {
						;
					}
				}
			}
			re.sign=-1;
		}
		return re;
	}

	public BigInteger multiply(BigInteger big)
	{
		BigInteger re = new BigInteger("");
		for(int i=0; i<100; i++) {

			for(int j=0; j<100; j++) {
				int x = this.num[i] * big.num[j];
				re.num[i+j] += x;
				re.num[i+j+1] += re.num[i+j]/10;
				re.num[i+j] = re.num[i+j] % 10;

			}
		}
		re.sign = this.sign*big.sign;
		return re;
	}

	@Override
	public String toString()
	{
		StringBuffer re = new StringBuffer();
		if(this.sign==-1) {
			re.append("-");
		}
		int i=0;
		while(i<200) {
			if(num[199-i]!=0) {
				break;
			}
			else {i+=1;}
		}
		for (int j=0; j<=199-i; j++) {
			re.append(Integer.toString(num[199-i-j]));
		}

		if(re.length()==0) {
			re.append("0");
		}
		String r = re.toString();
		return r;
	}

	static BigInteger evaluate(String input) throws IllegalArgumentException
	{
		Matcher m = EXPRESSION_PATTERN.matcher(input);
		if (m.find()==false) {
			throw new IllegalArgumentException();
		}
		String si1 = m.group(1);
		if (si1 == null) {si1 = "+";}
		String si2 = m.group(4);
		if (si2 == null) {si2 = "+";}
		int sign1 = Integer.parseInt(si1.replace(" ", "").replace("+", "1").replace("-", "-1"));
		int sign2 = Integer.parseInt(si2.replace(" ", "").replace("+", "1").replace("-", "-1"));
		BigInteger n1 = new BigInteger(m.group(2));
		BigInteger n2 = new BigInteger(m.group(5));
		BigInteger result = new BigInteger("");
		n1.sign=sign1;
		n2.sign=sign2;


		int op = Integer.parseInt(m.group(3).replace(" ", "").replace("+", "1").replace("-", "-1").replace("*", "0"));

		if(op==0) {
			result = n1.multiply(n2);
		}

		else if(sign1==1 && sign2 ==1) {
			if(op==1) {
				result = n1.add(n2);
			}
			if(op==-1) {
				result = n1.subtract(n2);
			}
		}

		else if(sign1==1 && sign2 ==-1) {
			if(op==1) {
				result = n1.subtract(n2);
			}
			if(op==-1) {
				result = n1.add(n2);
			}
		}

		else if(sign1==-1 && sign2 ==1) {
			if(op==1) {
				result = n2.subtract(n1);
			}
			if(op==-1) {
				result = n1.add(n2);
				result.sign=-1;
			}
		}
		else if(sign1==-1 && sign2 ==-1) {
			if(op==1) {
				result = n2.add(n1);
				result.sign=-1;
			}
			if(op==-1) {
				result = n2.subtract(n1);
			}
		}


		return result;

	}

	public static void main(String[] args) throws Exception
	{
		try (InputStreamReader isr = new InputStreamReader(System.in))
		{
			try (BufferedReader reader = new BufferedReader(isr))
			{
				boolean done = false;
				while (!done)
				{
					String input = reader.readLine();

					try
					{
						done = processInput(input);
					}
					catch (IllegalArgumentException e)
					{
						System.err.println(MSG_INVALID_INPUT);
					}
				}
			}
		}
	}

	static boolean processInput(String input) throws IllegalArgumentException
	{
		boolean quit = isQuitCmd(input);

		if (quit)
		{
			return true;
		}
		else
		{
			BigInteger result = evaluate(input);
			System.out.println(result.toString());

			return false;
		}
	}

	static boolean isQuitCmd(String input)
	{
		return input.equalsIgnoreCase(QUIT_COMMAND);
	}
}
