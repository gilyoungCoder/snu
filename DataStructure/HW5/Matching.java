import java.io.*;
import java.util.LinkedList;

public class Matching
{
	public static void main(String args[])
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		while (true)
		{
			try
			{
				String input = br.readLine();
				if (input.compareTo("QUIT") == 0)
					break;

				command(input);
			}
			catch (IOException e)
			{
				System.out.println("입력이 잘못되었습니다. 오류 : " + e.toString());
			}
		}
	}



	private static void command(String input) {
		if (input.isEmpty()){
			return;
		}
		switch (input.charAt(0)) {
			case '<':
				readfile(input.substring(2));
				break;
			case '@':
				slotprint(Integer.parseInt(input.substring(2)));
				break;
			case '?':
				PatternPlace(input.substring(2));
				break;
		}
	}


	public static Hashtable<StringKey, Page> hash = new Hashtable<>(100);

	private static void readfile(String path) {
		hash.clear();  //put(Key, Value)
		try {
			BufferedReader reader = new BufferedReader(new FileReader(path));
			String s;
			int line = 1; //1부터 시작
			while ((s = reader.readLine()) != null) {
				for (int i = 0; i <= s.length() - 6; i++) {

					StringKey key = new StringKey(s.substring(i , i +6));
					Page page = new Page(line, i+1);
					hash.insert(key, page);
				}
				line++;
			}

			reader.close();
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}

	public static class Hashtable<K extends Comparable <K>, V>{
		private AVLtree<K,V> [] table;

		public Hashtable(int n) {
			table = new AVLtree[n];
			for(int i= 0; i<n; i++) {
				table[i] = new AVLtree<>();
			}
		}

		public void insert(K key, V page) {
			int slot = key.hashCode()%table.length;
			table[slot].insert(key, page);
		}

		public void clear() {
			for(int i= 0; i<table.length; i++) {
				table[i] = new AVLtree<>();
			}
		}


	}

	public static class StringKey implements Comparable<StringKey>{

		public String key;

		public StringKey(String s){
			this.key = s;
		}

		public String toString(){
			return key;
		}

		public int hashCode(){
			int asci = 0;
			for(int i=0; i<key.length(); i++){
				asci += key.charAt(i);
			}
			return asci%100;
		}

		public int compareTo(StringKey s) {
			return this.key.compareTo(s.key);
		}

	}



	private static class Page{
		int line=0;
		int location=0;
		public Page(int line, int location){
			this.line=line;
			this.location = location;
		}
		public boolean equals(Page o) {
			return this.line==o.line && this.location==o.location;
		}
	}

	private static void slotprint(int i) {
		
		AVLtree tree = hash.table[i];
		if(tree.root == AVLtree.nullnode) {
			System.out.println("EMPTY");
		}

		else {
			tree.prePrint();
		}
	}

	private static void PatternPlace(String pattern) {
		String first = pattern.substring(0,6);
		StringKey fkey = new StringKey(first);
		LinkedList<Page> flist = hash.table[fkey.hashCode()].search(fkey).nlist;
		LinkedList<Page> rlist = new LinkedList<>();
		
		if(!flist.isEmpty()&&flist.get(0)!=null){
			for(Page item : flist){
				rlist.add(item);
			}
			for (int i = 1; i < pattern.length() - 5; i ++) {
				String piece = pattern.substring(i, i + 6);
				StringKey piecekey = new StringKey(piece);
				LinkedList<Page> plist = hash.table[piecekey.hashCode()].search(piecekey).nlist;
				for(Page item : flist) {
					Page check = new Page(item.line, item.location+i);
					
					
    				boolean contain=false;
    				for(Page item1 : plist){
    					if(check.equals(item1)) {
    						contain = true;
    					}
    				}
    				if(!contain) {
    					rlist.remove(item);
    				}
    				
					
				}
			}
		}

		String out = "";

		if(rlist.isEmpty()) {
			System.out.println("(0, 0)");
		}
		else {
			for(Page item : rlist) {
				out+="("+Integer.toString(item.line)+", "+Integer.toString(item.location)+") ";
			}
			System.out.println(out.trim());
		}

	}



	//Node<String, LinkedList<page>> root; (page 에는 줄, 위치 나옴 key는 string)




	private static class AVLtree <K extends Comparable <K>, V> {
		static final AVLNode nullnode = new AVLNode(null, null, null, null, 0);
		public AVLNode<K, V> root;
		public AVLNode<K, V> currnode = root;

		public AVLtree(){
			root = nullnode;
		}

 /*   	public AVLNode<K, V> search(K temp){
			if (root == nullnode){
				return nullnode;
			}

			else if(currnode.key.compareTo(temp)>0){
				currnode = currnode.leftchild;
				search(temp);
			}

			else if(currnode.key.compareTo(temp)<0){
				currnode = currnode.rightchild;
				search(temp);
			}

			else{
				AVLNode<K, V> tempnode = currnode;
				currnode = root;
				return tempnode;
			}
			return nullnode;
		}
*/

		public AVLNode<K ,V> search(K  key){
			return searchItem(root, key);
		}

		public AVLNode<K,V> searchItem(AVLNode t, K key){
			if(t == nullnode) {
				return nullnode;
			}
			else if (key.compareTo((K) t.key) == 0) {
				return t;
			}
			else if (key.compareTo((K) t.key) < 0){
				return searchItem(t.leftchild, key);
			}
			else{
				return searchItem(t.rightchild, key);
			}
		}
		
		public void insert(K temp, V page) {
			root = insertItem(root, temp, page);
		}

		public AVLNode<K,V> insertItem(AVLNode<K,V> tnode, K temp, V page){

			int tp;
			if (tnode == nullnode){
				tnode = new AVLNode<>(temp, page);
				return tnode;
			}

			int comp = tnode.key.compareTo(temp);
			
			if(comp>0){
				tnode.leftchild = insertItem(tnode.leftchild, temp, page);
				tnode.height = 1 + Math.max(tnode.rightchild.height, tnode.leftchild.height);
				tp = Dobalance(tnode);
				if (tp != NN){
					tnode = htbalance(tnode, tp);
				}
			}

			else if(comp<0){
				tnode.rightchild = insertItem(tnode.rightchild, temp, page);
				tnode.height = 1 + Math.max(tnode.rightchild.height, tnode.leftchild.height);
				tp = Dobalance(tnode);
				if (tp != NN){
					tnode = htbalance(tnode, tp);
				}
			}

			else{	
				if(comp == 0){
					tnode.nlist.add(page);
				}					// key 값 같은데 널이 아닌경우
			}

			return tnode;
		}


		private final int LL = 1, LR =2, RR = 3, RL = 4, NN = 0;

		private int Dobalance (AVLNode <K,V> tnode) {
			int type = -1;
			if ( tnode.leftchild.height+2 <= tnode.rightchild.height) {
				if ( (tnode.rightchild.leftchild.height) <= (tnode.rightchild.rightchild.height)) {
					type = RR;
				}
				else {
					type= RL;
				}
			}
			else if ( tnode.leftchild.height >= tnode.rightchild.height+2 ) {
				if ( tnode.leftchild.leftchild.height >= tnode.leftchild.rightchild.height) {
					type = LL;
				}
				else {
					type = LR;
				}
			}
			else {
				type = NN;
			}
			return type;
		}


		private AVLNode<K,V> htbalance(AVLNode<K, V> tnode, int type) {
			AVLNode<K,V> rnode = nullnode;
			switch (type) {
				case LL:
					rnode = rightRotate(tnode);
					break;

				case LR:
					tnode.leftchild = leftRotate(tnode.leftchild);
					rnode = rightRotate(tnode);
					break;

				case RR:
					rnode = leftRotate(tnode);
					break;

				case RL:
					tnode.rightchild = rightRotate(tnode.rightchild);
					rnode = leftRotate(tnode);
					break;

				default:
					rnode = tnode;
					break;
			}
			return rnode;
		}

		private AVLNode<K,V> leftRotate(AVLNode tnode) {

			if(tnode.rightchild == nullnode) {
				return nullnode;
			}

			AVLNode<K,V> Rchild = tnode.rightchild;
			AVLNode<K,V> RLchild = Rchild.leftchild;
			Rchild.leftchild = tnode;
			tnode.rightchild = RLchild;

			tnode.height = 1 + Math.max(tnode.leftchild.height, tnode.rightchild.height);
			Rchild.height = 1 + Math.max(Rchild.leftchild.height, Rchild.rightchild.height);

			return Rchild;
		}

		private AVLNode<K,V> rightRotate(AVLNode<K,V> tnode) {

			if(tnode.leftchild == nullnode) {
				return nullnode;
			}

			AVLNode<K,V> Lchild = tnode.leftchild;
			AVLNode<K,V> LRchild = Lchild.rightchild;
			Lchild.rightchild = tnode;
			tnode.leftchild = LRchild;

			tnode.height = 1 + Math.max(tnode.leftchild.height, tnode.rightchild.height);
			Lchild.height = 1 + Math.max(Lchild.leftchild.height, Lchild.rightchild.height);

			return Lchild;
		}

		static String avl = "";
		public void preOrder(AVLNode<K,V> root) {
			if(root != nullnode) {
				avl+=root.key.toString()+" ";
				preOrder(root.leftchild);
				preOrder(root.rightchild);
			}
		}

		public void prePrint() {
			avl = "";
			preOrder(root);
			avl = avl.substring(0, avl.length()-1);
			System.out.print(avl);
			System.out.println();
		}

		

	}//tree end


	public static class AVLNode <K extends Comparable <K>, V>{
		public AVLNode<K,V> leftchild;
		public AVLNode<K,V> rightchild;
		public int height;
		public K key;
		public LinkedList<V> nlist = new LinkedList<>();
		
		public AVLNode(K x, V y){
			key = x;
			nlist.add(y);
			leftchild = AVLtree.nullnode;
			rightchild = AVLtree.nullnode;
			height = 1;
		}

		public AVLNode(K x, V y, AVLNode<K,V> a, AVLNode<K,V> b, int h){
			key = x;
			nlist.add(y);
			leftchild = a;
			rightchild = b;
			height = h;
		}
	}


}
