import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.ArrayList;


public class Subway {

    static HashMap<String, stat> vertex = new HashMap<>();  // id, 역
    static HashMap<String, ArrayList<String>> transid = new HashMap<>(); //이름, id list
    static HashMap<String, Integer> heapIndex = new HashMap<>();

    static final long max = Long.MAX_VALUE;

    public static void main(String[] args) {

        String file = args[0];
        buildGraph(file);

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

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




    }//end main

    public static void buildGraph(String file) {
        try {
            InputStreamReader fileIn = new InputStreamReader(new FileInputStream(file), "UTF-8");
            //FileReader freader = new FileReader(file);
            BufferedReader br = new BufferedReader(fileIn);

            vertex.clear();

            while(true) {
                String data = br.readLine();
                if(data.equals("")) { //"\n"
                    break;
                }
                String[] statDat = data.split(" ");
                String id = statDat[0];
                String name = statDat[1];
                String line = statDat[2];
                stat nstat = new stat(id, name, line);
                vertex.put(id, nstat);


                if(!transid.containsKey(name)) {
                    ArrayList<String >tlist = new ArrayList<>();
                    tlist.add(id);
                    transid.put(name, tlist);
                }
                else {
                    edge nedge = new edge(nstat, 5);
                    for(String alreadyId : transid.get(name)) {
                        stat alreadyStat = vertex.get(alreadyId);
                        alreadyStat.insertEdge(nedge);
                        nstat.insertEdge(new edge(alreadyStat, 5));
                    }
                    transid.get(name).add(id); //edge 추가 후, 삽입
                }
            }


            while(true) {
                String data = br.readLine();
                if(data==null) {
                    break;
                }
                String[] edgedata = data.split(" ");
                edge nedge = new edge(vertex.get(edgedata[1]), Integer.parseInt(edgedata[2]));
                vertex.get(edgedata[0]).insertEdge(nedge);
            }
            fileIn.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }//build end

    static void command(String input) {
        try{
            PrintStream out = new PrintStream(System.out, true, "UTF-8");
            System.setOut(out);
            //BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
            npath shortest = new npath();
            String[] com = input.split(" ");
            String shortId="";
            for (String idF : transid.get(com[0])) {
                npath npath = findPath(idF, com[1]);
                if(shortest.time>npath.time) {
                    shortest = npath;
                    shortId = idF;
                }
            }
            
            Dijkstra(vertex, vertex.get(shortId));
            stat curr = vertex.get(shortest.id);
            String result="";
            String station="";
            while(curr.prev!=null) {
                if(curr.prev.name.equals(curr.name)) {
                    station = " ["+curr.name+"]"; 
                    curr = curr.prev;
                }
                else {
                    station = " "+curr.name;
                }
                result = station+result;
                curr=curr.prev;
            }
            result = curr.name+result;
            System.out.println(result);
            System.out.println(shortest.time);
            // try{
            //     bw.write(result+"\n");				
            //     bw.write(Long.toString(shortest.time)+"\n");
            //     bw.flush();
            // }
            // catch(Exception e){
            //     e.printStackTrace();
            // }

            //System.out.println(result);
            //System.out.println(shortest.time);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
    
 
    static class npath{
       String id;
       long time;
       npath(String id, long time){
          this.id=id;
          this.time=time;
       }
       npath(){
          time = max;
       }
    }
    
    static npath findPath(String fromId, String toName) {
        Dijkstra(vertex, vertex.get(fromId));
        
        long mincost = max;
        String toId="";
        for (String id : transid.get(toName)) {
            if (mincost > vertex.get(id).cost) {
                mincost = vertex.get(id).cost;
                toId = id;
            }
        }
        // 최단 경로 찾기 다익스트라...

        return new npath(toId, mincost);
    }
    
    static void Dijkstra(HashMap<String, stat> vertex, stat r) {
        //HashMap<String, stat> s = new HashMap<>();
        //HashMap<String, stat> currVertex = (HashMap<String, stat>)vertex.clone();
        stat [] array = vertex.values().toArray(new stat[vertex.size()]);
        Heap curr = new Heap(array, vertex.size());
        heapIndex.clear();
        
        for (HashMap.Entry<String, stat> entry : vertex.entrySet()){
            entry.getValue().cost = max;
            entry.getValue().prev = null;
        }
        //s.put(r.id, r);
        r.cost = 0;  // 레퍼런스만 가리키고 있는지 확인 (hash, heap)
        curr.buildHeap();
        curr.deleteMin();
        for (int i=0; i<curr.numItems; i++){
            heapIndex.put(curr.A[i].id, i);
        }

        for (edge nextstat : r.nextstat) {
            nextstat.dest.cost = nextstat.min;
            //if(!nextstat.dest.name.equals(r.name)){  //맨처음에는 절대 환승하지 않으므로 prev 설정 x
            nextstat.dest.prev = r;
            //}
            curr.percolateUp(heapIndex.get(nextstat.dest.id));
        }

        while(curr.numItems != 0) {
            stat u = curr.deleteMin();
            for (edge nextstat : u.nextstat) {
                if (nextstat.dest.cost > u.cost + nextstat.min) {
                    nextstat.dest.cost = u.cost + nextstat.min;
                    curr.percolateUp(heapIndex.get(nextstat.dest.id));
                    nextstat.dest.prev = u;
                }
            }
        }
    }
/*
   static void deleteMin(HashMap<String, stat> vertex) {//수정

      long min = Long.MAX_VALUE;
      String minkey = null;
      for (HashMap.Entry<String, stat> entry : vertex.entrySet()){
         if(min>entry.getValue().cost) {
            min = entry.getValue().cost;
            minkey = entry.getKey();
         }
       }
      vertex.remove(minkey);
   }
   */

    static class path{
        ArrayList<String> pathId;
        long time;
        public path() {
            pathId = null;
            time = max;
        }
        public path(String dest){
            pathId = new ArrayList<>();
            pathId.add(dest);
            time = max;
        }
    }

    static class stat  {
        String id;
        String name;
        ArrayList<edge> nextstat;
        String line;
        long cost;
        stat prev;

        public stat(String id, String name, String line){
            nextstat = new ArrayList<>();
            cost = max;
            prev = null;
            this.id = id;
            this.name = name;
            this.line = line;
        }

        public void insertEdge(edge ed) {
            nextstat.add(ed);
        }
        
        /*
        public void print() {
            System.out.print(this.name);
        }
      
      public int compareTo(stat other) {  // Heap 오버라이딩
             return Long.compare(this.cost, other.cost);
         }
         */
    }//station class end

    static class edge{
        stat dest;
        long min;
        public edge(stat dest, long min) {
            this.dest = dest;
            this.min = min;
        }
    }//edge class

    static class Heap{
        public stat[] A;
        private int numItems;
        public Heap(int arraySize) {
            A = new stat[arraySize];
            numItems = 0;
        }
        public Heap(stat[] B, int numElements) {
            A = B; // 배열 레퍼런스 복사
            numItems = numElements;
        }
        public void insert(stat newItem){// 힙 A[0...numItems-1]에 원소 newItem을 삽입한다(추가한다)
            if (numItems < A.length) {
                A[numItems] = newItem;
                percolateUp(numItems);
                numItems++;
            }
        }
        public stat deleteMin(){ // 힙 A[0...numItems-1]에서 최솟값을 삭제하면서 리턴한다
            if (!isEmpty()) {
                stat min = A[0];
                A[0] = A[numItems-1];
                numItems--;
                percolateDown(0);
                return min;
            }
            else {
                return null;
            }
        }
        public void buildHeap() {
            if (numItems >= 2) {
                for (int i = (numItems-2)/2; i >= 0; i--){
                    percolateDown(i);
                }
            }
        }
        public boolean isEmpty() { // 힙이 비어있는지 알려준다
            return numItems == 0;
        }
        public void clear() {
            A = new stat[A.length];
            numItems = 0;
        }

        private void percolateUp(int i) {// A[i]에서 시작해서 힙성질을 만족하도록 수선한다 // A[0...i-1]은 힙성질을 만족하고 있음
            int parent = (i-1)/2;
            if (parent >= 0 && A[i].cost<A[parent].cost) {
               stat tmp = A[i];
                A[i] = A[parent];
                A[parent] = tmp;
                heapIndex.replace(A[parent].id, parent);
                heapIndex.replace(A[i].id, i);
                percolateUp(parent);
            }
        }



        private void percolateDown(int i) {// A[i]를 루트로 스며내리기 // A[n]: last item, boundary
            int minchild = 2*i + 1; // left child
            int rightChild = 2*i + 2; // right child
            if (minchild <= numItems-1) {
                if (rightChild <= numItems-1 && A[minchild].cost > A[rightChild].cost) {
                    minchild = rightChild; // index of larger child
                }
                if (A[i].cost > A[minchild].cost) {
                    stat tmp = A[i];
                    A[i] = A[minchild];
                    A[minchild] = tmp;
                    heapIndex.replace(A[minchild].id, minchild);
                    heapIndex.replace(A[i].id, i);
                    percolateDown(minchild);
                }
            }
        }
    }


}//heap searching 개선