import java.io.*;
import java.util.*;

public class SortingTest
{
	public static void main(String args[])
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		try
		{
			boolean isRandom = false;	// 입력받은 배열이 난수인가 아닌가?
			int[] value;	// 입력 받을 숫자들의 배열
			String nums = br.readLine();	// 첫 줄을 입력 받음
			if (nums.charAt(0) == 'r')
			{
				// 난수일 경우
				isRandom = true;	// 난수임을 표시

				String[] nums_arg = nums.split(" ");

				int numsize = Integer.parseInt(nums_arg[1]);	// 총 갯수
				int rminimum = Integer.parseInt(nums_arg[2]);	// 최소값
				int rmaximum = Integer.parseInt(nums_arg[3]);	// 최대값

				Random rand = new Random();	// 난수 인스턴스를 생성한다.

				value = new int[numsize];	// 배열을 생성한다.
				for (int i = 0; i < value.length; i++)	// 각각의 배열에 난수를 생성하여 대입
					value[i] = rand.nextInt(rmaximum - rminimum + 1) + rminimum;
			}
			else
			{
				// 난수가 아닐 경우
				int numsize = Integer.parseInt(nums);

				value = new int[numsize];	// 배열을 생성한다.
				for (int i = 0; i < value.length; i++)	// 한줄씩 입력받아 배열원소로 대입
					value[i] = Integer.parseInt(br.readLine());
			}

			// 숫자 입력을 다 받았으므로 정렬 방법을 받아 그에 맞는 정렬을 수행한다.
			while (true)
			{
				int[] newvalue = (int[])value.clone();	// 원래 값의 보호를 위해 복사본을 생성한다.

				String command = br.readLine();

				long t = System.currentTimeMillis();
				switch (command.charAt(0))
				{
					case 'B':	// Bubble Sort
						newvalue = DoBubbleSort(newvalue);
						break;
					case 'I':	// Insertion Sort
						newvalue = DoInsertionSort(newvalue);
						break;
					case 'H':	// Heap Sort
						newvalue = DoHeapSort(newvalue);
						break;
					case 'M':	// Merge Sort
						newvalue = DoMergeSort(newvalue);
						break;
					case 'Q':	// Quick Sort
						newvalue = DoQuickSort(newvalue);
						break;
					case 'R':	// Radix Sort
						newvalue = DoRadixSort(newvalue);
						break;
					case 'X':
						return;	// 프로그램을 종료한다.
					default:
						throw new IOException("잘못된 정렬 방법을 입력했습니다.");
				}
				if (isRandom)
				{
					// 난수일 경우 수행시간을 출력한다.
					System.out.println((System.currentTimeMillis() - t) + " ms");
				}
				else
				{
					// 난수가 아닐 경우 정렬된 결과값을 출력한다.
					for (int i = 0; i < newvalue.length; i++)
					{
						System.out.println(newvalue[i]);
					}
				}

			}
		}
		catch (IOException e)
		{
			System.out.println("입력이 잘못되었습니다. 오류 : " + e.toString());
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	private static int[] DoBubbleSort(int[] value)
	{
		// TODO : Bubble Sort 를 구현하라.
		// value는 정렬안된 숫자들의 배열이며 value.length 는 배열의 크기가 된다.
		// 결과로 정렬된 배열은 리턴해 주어야 하며, 두가지 방법이 있으므로 잘 생각해서 사용할것.
		// 주어진 value 배열에서 안의 값만을 바꾸고 value를 다시 리턴하거나
		// 같은 크기의 새로운 배열을 만들어 그 배열을 리턴할 수도 있다.
		int len = value.length;
		for(int i=0; i<value.length-1; i++) {

			for(int j=0; j<len-1; j++) {

				if (value[j] > value[j+1]) {
					int a = value[j+1];
					value[j+1] = value[j];
					value[j] = a;
				}
			}

			len--;
		}

		return (value);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	private static int[] DoInsertionSort(int[] value)
	{
		// TODO : Insertion Sort 를 구현하라.

		for(int i=1; i<value.length; i++) {
			int temp = value[i];
			int j;
			for(j=i-1; j>=0; j--) {

				if (temp >= value[j]) {
					break;
				}

				else {
					value[j+1] = value[j];
				}

			}
			value[j+1] = temp;
		}

		return (value);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////

	private static int[] DoHeapSort(int[] value)
	{
		// TODO : HeapSort 를 구현하라.
		for(int i = (value.length-2)/2; i>=0; i--){
			branch(value, i, value.length);
		}

		for(int j=1; j<=value.length-1; j++){
			int temp = value[value.length-j];
			value[value.length-j] = value[0];
			value[0] = temp;
			branch(value, 0, value.length-j);
		}

		return (value);
	}

	private static int[] branch(int [] array, int node, int len){
		int parent =node;

		while(2*parent+1 < len){
			int maxchild = 2*parent+1;

			if(maxchild+1 < len && array[maxchild] < array[maxchild+1]){
				++maxchild;
			}

			if(array[parent] >= array[maxchild]){
				break;
			}

			int temp = array[parent];
			array[parent] = array[maxchild];
			array[maxchild] = temp;
			parent = maxchild;
		}
		return array;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	private static int[] DoMergeSort(int[] value)
	{
		// TODO : Merge Sort 를 구현하라.
		int[] nvalue= value.clone();
		msort(0, value.length-1, value, nvalue);
		return (value);
	}

	private static void merge(int left, int right, int mid, int[] value, int[] nvalue) {
		int i = left;
		int j = mid+1;
		int k = left;
		while(i<=mid && j<=right) {
			int min = value[i] < value[j] ? value[i++] : value[j++];
			nvalue[k++] = min;
		}

		if(i==mid+1) {
			i=j;
		}

		while (k<=right) {
			nvalue[k++] = value[i++];
		}

	}

	private static void msort(int left, int right, int[] value, int nvalue[]) {
		if (left >= right) {
			return ;
		}
		int mid = (left+right)/2;


		msort(left, mid, nvalue, value);  //주배열과 보조배열이 번갈아 가며 역할을 수행할 수 있도록 함.
		msort(mid+1, right, nvalue, value);
		merge(left, right, mid, nvalue, value);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	private static int[] DoQuickSort(int[] value)
	{
		// TODO : Quick Sort 를 구현하라.
		qsort(0, value.length-1, value);
		return (value);
	}
	private static void qsort(int left, int right, int[] value ) {
		if (left>=right) {
			return ;
		}

		int p = value[right];
		int k = right-1;

		for (int i=right-1; i>=left; i--) {
			if(value[i]>p) {
				int item = value[k--];
				value[k+2] =  value[i];
				value[i] = item;
			}
		}
		value[k+1]=p;
		qsort(left, k, value);
		qsort(k+2, right, value);
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////
	private static int[] DoRadixSort(int[] value)
	{
		// TODO : Radix Sort 를 구현하라.
		int [] sorvalue = new int [value.length];
		int [] freq = new int[19];


		int max = value[0];
		int min = value[0];
		for(int i =0; i<value.length; i++) {
			if(value[i] > max) {
				max = value[i];
			}
			if(value[i] < min) {
				min = value[i];
			}
		}

		int digits = 0;
		int digitss=0;
		while(max!=0) {
			max /= 10;
			++digits;
		}

		while(min!=0) {
			min /= 10;
			++digitss;
		}

		digits = (digits >= digitss) ? digits : digitss; // 최대 원소와 최소 원소 중 자릿수가 더 큰 값을 저장(음수의 자릿수가 최대일 수 있으므로)

		for(int j=1; j<=digits; j++) {
			digitsort(value, sorvalue, freq, j);
			int [] newvalue=value;
			value=sorvalue;
			sorvalue=newvalue;
		}

		return (value);
	}




	private static int [] digitsort(int [] value, int[] sorvalue, int[] freq, int digit) {

		for(int i=0; i<19; i++) {
			freq[i]=0;
		}

		for(int i=0; i<value.length; i++) {
			freq[(int)(value[i]/Math.pow(10, digit-1))%10+9]++;
		}

		for(int i=1; i<19; i++) {
			freq[i] += freq[i-1];
		}

		for(int i=value.length-1; i>=0; i--) {
			sorvalue[--freq[(int)(value[i]/Math.pow(10, digit-1))%10+9]] = value[i];
		}

		return sorvalue;
	}

}



