
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class main {

    public static int duplicateBooks = 0;

    public static Map<Integer,DfsReturn> cache = new HashMap<>();
    public static Map<Integer, Set<Integer>> bookCosts = new HashMap<>();
    public static Map<Integer, Boolean> booksOpt = new HashMap<>();
    public static Map<Integer, Integer> serverPassages = new HashMap<>();
    public static Map<Integer, Integer> booksFromServer = new HashMap<>();
    public static Set<Integer> notTookBooks = new HashSet<>();
    public static List<DfsReturn> paths = new ArrayList<>();

    public static class Server {

        int id;
        Set<Integer> books = new HashSet<>();
        Map<Integer,Integer> nextServers = new HashMap<>();
    }

    public static class DfsReturn {
        int cost;
        List<Integer> serverPath;
        Set<Integer> books;
    }

    public static String determineBestPath(Map<Integer, Server> servers, Set<Integer> visitedBooks, List<Integer> t, int turn) {

        serverPassages.remove(0);
        boolean done = !serverPassages.values().stream().anyMatch(i -> i>0);

        DfsReturn res = dfs(0,0,0,servers,new HashSet<>(),visitedBooks,new ArrayList<>(),new HashSet<>(), Integer.MAX_VALUE-1, turn, done);
        paths.add(res);

        String result = "";
        int totalBooks = 0;
        for (Integer s : res.serverPath) {
            serverPassages.putIfAbsent(s,0);
            if (turn == 0) {serverPassages.put(s,serverPassages.get(s)+1);};
            if (turn == 1) {serverPassages.put(s,serverPassages.get(s)-1);};
            cache.remove(s);
            result = result + s + " ";
            long possBooks = servers.get(s).books.stream().filter(b -> !visitedBooks.contains(b)).count();
            if (possBooks+totalBooks<=10) {
                for (Integer book : servers.get(s).books) {
                    if (!visitedBooks.contains(book) && totalBooks<10) {
                        visitedBooks.add(book);
                        booksFromServer.put(s,booksFromServer.getOrDefault(s,0)+1);
                        result = result + book + " ";
                        totalBooks++;
                        if (notTookBooks.contains(book)) {notTookBooks.remove(book);}
                    }
                    else {
                        notTookBooks.add(book);
                    }
                }
            }
            else {
                List<Integer> bS = servers.get(s).books.stream().sorted(Comparator.comparingInt((b-> getSecondCost(b,res.cost)))).toList();
                for (int i=bS.size()-1;i>=0;i--) {
                    Integer book = bS.get(i);
                    if (!visitedBooks.contains(book) && totalBooks<10) {
                        visitedBooks.add(book);
                        booksFromServer.put(s,booksFromServer.getOrDefault(s,0)+1);
                        result = result + book + " ";
                        totalBooks++;
                        duplicateBooks++;
                        if (notTookBooks.contains(book)) {notTookBooks.remove(book);}
                    }
                    else {
                        notTookBooks.add(book);
                    }
                }
            }

        }

        t.add(res.cost);
        return result;
    }

    public static Integer getSecondCost(Integer book, Integer cost) {

        if (bookCosts.get(book) == null || bookCosts.get(book).isEmpty()) {
            return Integer.MAX_VALUE;
        }

        Integer min = Integer.MAX_VALUE;
        for (Integer c : bookCosts.get(book)) {
            if (c != cost && c<min) {
                min = c;
            }
        }

        return min;

    }

    public static DfsReturn dfs(int sId, int bookTotal, int cost, Map<Integer, Server> servers, Set<Integer> visitedServers, Set<Integer> visitedBooks, List<Integer> serverPath, Set<Integer> books, Integer min, int turn, boolean done) {

        /*if (cache.containsKey(sId)) {
            if (books.stream().anyMatch(visitedBooks::contains)) {
                cache.remove(sId);
            }
            else {
                return cache.get(sId);
            }
        }*/
        if (cost>min) {
            DfsReturn ret = new DfsReturn();
            ret.cost = Integer.MAX_VALUE;
            return ret;
        }


        visitedServers.add(sId);
        serverPath.add(sId);

        int bookTotalAtFirst = bookTotal;
        Set<Integer> noBooks = new HashSet<>();
        long booksInServer = servers.get(sId).books.stream().filter(b -> !visitedBooks.contains(b) && !books.contains(b)).count();


        if (turn==0 || booksInServer+bookTotal<=10) {
            for (Integer book : servers.get(sId).books) {
                bookCosts.putIfAbsent(book,new HashSet<>());
                bookCosts.get(book).add(cost);
                if (bookTotal<10 && !visitedBooks.contains(book) && !books.contains(book)) {
                    books.add(book);
                    bookTotal++;
                }
                else {
                    noBooks.add(book);
                }
            }
        }
        else {

            for (Integer book : servers.get(sId).books) {
                if (bookTotal<10 && !visitedBooks.contains(book) && !books.contains(book) && booksOpt.getOrDefault(book, true)) {
                    books.add(book);
                    bookTotal++;
                    booksOpt.put(book,true);
                }
            }
            if (bookTotal<10) {
                for (Integer book : servers.get(sId).books) {
                    if (bookTotal<10 && !visitedBooks.contains(book) && !books.contains(book)) {
                        books.add(book);
                        bookTotal++;
                        booksOpt.put(book,true);
                    }
                }
            }

        }

        if (turn!=1) {
            if (bookTotal<10 && bookTotalAtFirst+servers.get(sId).books.size()>=10) {
                for (Integer b : noBooks) {
                    booksOpt.put(b,false);
                }
            }
        }

        if (bookTotal == 10) {
            DfsReturn ret = new DfsReturn();
            ret.cost = cost;
            ret.serverPath = serverPath;
            ret.books = books;
            cache.put(sId, ret);
            return ret;
        }

        Integer minCost = min;
        DfsReturn result = new DfsReturn();
        result.cost = min+1;
        for (Integer sTo : servers.get(sId).nextServers.keySet()) {
            if (!visitedServers.contains(sTo)) {

                int costTo = servers.get(sId).nextServers.get(sTo);
                DfsReturn res = dfs(sTo, bookTotal, (cost+costTo), servers, new HashSet<>(visitedServers), visitedBooks, new ArrayList<>(serverPath), new HashSet<>(books), minCost, turn, done);
                if (res.cost<minCost) {
                    result = res;
                    minCost = res.cost;
                }
            }
        }


        if (result.serverPath != null) {
            Integer sTo = result.serverPath.get(result.serverPath.size()-1);
            Set<Integer> bookTo = result.books;
            long booksNInServer = servers.get(sTo).books.stream().filter(b -> bookTo.contains(b)).count();
            int costTo = servers.get(result.serverPath.get(result.serverPath.size()-2)).nextServers.get(sTo);

            if (booksNInServer != 0 && booksNInServer<=2 && costTo / booksNInServer > 3.0) {
                result.serverPath = result.serverPath.subList(0,result.serverPath.size()-2);
                for (Integer b : servers.get(sTo).books.stream().filter(b -> bookTo.contains(b)).toList()) {
                    result.books.remove(b);
                }
                return result;
            }
        }
        return result;
    }

    public static void main(String[] args) throws Exception {

        Map<Integer, Server> servers = new HashMap<>();
        Server s0 = new Server();
        s0.id = 0;
        servers.put(0,s0);
        int serverCount = 0;

        BufferedReader br = new BufferedReader(new FileReader("./resources/servers"));
        String line = br.readLine();

        while (line != null) {

            if (serverCount < 2499) {
                Server server = new Server();
                String[] fileLine = line.split(" ");
                Set<Integer> files = new HashSet<>();
                boolean first = true;
                for (String s : fileLine) {
                    if (first) {
                        first = false;
                        continue;
                    }
                    else {
                        files.add(Integer.parseInt(s));
                    }
                }
                server.id = Integer.parseInt(fileLine[0]);
                server.books = files;
                servers.put(server.id, server);
                serverCount++;
            }
            else {
                String[] serverCoLine = line.split(" ");
                Integer s1 = Integer.parseInt(serverCoLine[0]);
                Integer s2 = Integer.parseInt(serverCoLine[1]);
                Integer cost = Integer.parseInt(serverCoLine[2]);
                if (cost<30 && servers.containsKey(s1) && servers.containsKey(s2)) {
                    if (servers.get(s1).nextServers.get(s2) == null  || cost<servers.get(s1).nextServers.get(s2)) {
                        servers.get(s1).nextServers.put(s2, cost);
                    }
                    if (servers.get(s2).nextServers.get(s1) == null  || cost<servers.get(s2).nextServers.get(s1)) {
                        servers.get(s2).nextServers.put(s1, cost);
                    }
                }
            }
            line = br.readLine();
        }
        br.close();

        int t = 0;
        Set<Integer> visitedBooks = new HashSet<>();
        String result = "";
        Integer number = 0;
        while (t<3600) {

            List<Integer> cost = new ArrayList<>();
            String s = determineBestPath(servers, visitedBooks, cost, 0);
            result = result + s;
            t = t+cost.get(0);
            number++;
            System.out.println(number + " : ");
            System.out.println(t);
            System.out.println(result);
        }


        /*t = 0;
        visitedBooks = new HashSet<>();
        result = "";
        number = 0;
        while (t<3600) {

            List<Integer> cost = new ArrayList<>();
            String s = determineBestPath(servers, visitedBooks, cost, 1);
            result = result + s;
            t = t+cost.get(0);
            number++;
            System.out.println(number + " : ");
            System.out.println(t);
            System.out.println(result);
        }*/

        List<DfsReturn> filtered = paths.stream().filter(p -> {
            int s = p.serverPath.get(p.serverPath.size()-1);
            return servers.get(s).books.stream().filter(b->p.books.contains(b)).count()<=2;
        })
                        .toList();
        System.out.println(visitedBooks.size());
        System.out.print(result);















        /*// collect book scores
        Map<Integer, Integer> timePerBook = new TreeMap<>();
        Map<Integer, List<Integer>> pathToBook = new HashMap<>();

        Set<Integer> loadedBooks = new HashSet<>();
        Integer iter = 100;
        while(iter>0) {
            Integer currServer = 0;
            int t=0;
            boolean notDoneYet = true;
            List<Integer> currPath = new ArrayList<>();
            currPath.add(0);
            while (t<3600) {
                //add books
                if (serverFiles.get(currServer) != null) {
                    for (Integer book : serverFiles.get(currServer)) {
                        if (!loadedBooks.contains(book)) {
                            loadedBooks.add(book);
                            timePerBook.put(book, t);
                            pathToBook.put(book, currPath);
                        }
                        else if (t<timePerBook.get(book)) {
                            //better path than already found
                            timePerBook.put(book, t);
                            pathToBook.put(book, currPath);
                        }
                    }
                }

                //determineNextServer();
                Integer serverTo = 0;

                    Double maxScore = null;
                    for (Integer s : serverList.get(currServer)) {
                        //(Math.random() * 100)
                        long availableBooks = serverFiles.get(s) == null ? 0 : serverFiles.get(s).stream().filter(f -> !loadedFiles.contains(f)).count();
                        Double sScore = (double) Math.max(availableBooks,1) * bookCoeff / serverDelay.getOrDefault(s,serverConnexions.get(Connexion.of(s, currServer)));
                        if (maxScore == null || sScore > maxScore) {
                            maxScore = sScore;
                            serverTo = s;
                        }
                    }
                    Integer cnxCost = serverConnexions.get(Connexion.of(serverTo, currServer));
                    t = t + cnxCost;
                    tBeforeReset = tBeforeReset + cnxCost;
                    result = result + " " + serverTo;
                    currServer = serverTo;
                    serverChain.add(serverTo);


            }


        }*/



        /*Integer iter = 10;
        String finalResult = "";
        Integer points = 0;
        while(iter>0) {
            int t = 0;
            String result = "";
            Integer currServer = 0;
            Set<Integer> loadedFiles = new HashSet<>();
            Integer currFiles = 0;
            Integer totalPoints = 0;
            Map<Integer, Integer> serverDelay = new HashMap<>();
            for (Integer s : serverList.get(0)) {
                serverDelay.put(s, serverConnexions.get(Connexion.of(0,s)));
            }
            List<Integer> serverChain = new ArrayList<>();
            serverChain.add(0);
            Integer tBeforeReset = 0;
            while (t < 3600) {

                //addFiles();
                if (serverFiles.get(currServer) != null) {
                    for (Integer file : serverFiles.get(currServer)) {
                        if (!loadedFiles.contains(file) && currFiles < 10) {
                            loadedFiles.add(file);
                            currFiles++;
                            result = result + " " + file;
                            totalPoints++;
                        }
                    }
                }
                //determineNextServer();
                Integer serverTo = 0;
                if (currFiles < 10) {
                    Double minScore = null;
                    for(Integer s : serverList.get(currServer)) {
                        long availableBooks = serverFiles.get(s) == null ? 0 : serverFiles.get(s).stream().filter(f -> !loadedFiles.contains(f)).count();
                        Double score = (double) serverConnexions.get(Connexion.of(currServer, s))/availableBooks;
                        while(availableBooks == 0) {

                        }
                        if (minScore == null || score<minScore) {
                            minScore = score;
                            serverTo = s;
                        }
                    }
                    Double maxScore = null;
                    for (Integer s : serverList.get(currServer)) {
                        //(Math.random() * 100)
                        long availableBooks = serverFiles.get(s) == null ? 0 : serverFiles.get(s).stream().filter(f -> !loadedFiles.contains(f)).count();
                        Double sScore = (double) Math.max(availableBooks,1) * bookCoeff / serverDelay.getOrDefault(s,serverConnexions.get(Connexion.of(s, currServer)));
                        if (maxScore == null || sScore > maxScore) {
                            maxScore = sScore;
                            serverTo = s;
                        }
                    }
                    Integer cnxCost = serverConnexions.get(Connexion.of(serverTo, currServer));
                    t = t + cnxCost;
                    tBeforeReset = tBeforeReset + cnxCost;
                    result = result + " " + serverTo;
                    currServer = serverTo;
                    serverChain.add(serverTo);
                } else {
                    result = result + "0";
                    currServer = 0;
                    currFiles = 0;
                    Integer delay = tBeforeReset;
                    for (int i=0;i<serverChain.size()-1;i++) {
                        Integer s = serverChain.get(i);
                        serverDelay.put(s, delay);
                        delay = delay - serverConnexions.get(Connexion.of(s, serverChain.get(i+1)));
                    }
                    serverChain = new ArrayList<>();
                    serverChain.add(0);
                    tBeforeReset = 0;
                }


                //goToNextServer();

            }
            iter--;
            if (points<totalPoints) {
                finalResult = result;
                points=totalPoints;
            }
        }
        System.out.println(finalResult);
        System.out.println(points);*/




    }

    void findAdresse() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader("./resources/adresses.txt"));
        String line = br.readLine();
        long max = 0;
        String maxAddresse = null;

        while (line != null) {
            long consommation = Long.parseLong(line.split(" ")[0]);
            String address = line.substring(line.indexOf(" ") + 1);
            if (consommation > max) {
                max = consommation;
                maxAddresse = address;
            }

            line = br.readLine();
        }
        br.close();
        System.out.println(maxAddresse);
    }

    void findDigicode() throws Exception{
        BufferedReader br = new BufferedReader(new FileReader("./resources/digicode"));
        String line = br.readLine();
        String[] last4 = new String[4];
        Long digicode = null;

        while (line != null) {

            if (line.contains("4")) {
                long currDigicode = Long.parseLong(last4[0]);
                if (currDigicode%2==0 && currDigicode%7==0 && last4[0].length()==6
                        && !last4[1].contains("3") && !last4[2].contains("3") && !last4[3].contains("3")) {
                    digicode=currDigicode;
                    break;
                }
            }

            last4[3] = last4[2];
            last4[2] = last4[1];
            last4[1] = last4[0];
            last4[0] = line;
            line = br.readLine();
        }

        System.out.println(digicode);
        br.close();

        Long l1 = 181637366l;
        Long l2 = 144042748l;
        Long l3 = 661974207l;
        Long m = 987654321l;

        System.out.println((((l1*l2)%m)*l3)%m);
    }

    void findMdp() throws Exception{
        BufferedReader br = new BufferedReader(new FileReader("./resources/mdp"));
        String line = br.readLine();
        List<Long> list = new ArrayList<>();

        while (line != null) {
            list.add(Long.parseLong(line));
            line = br.readLine();
        }

        br.close();

        Pair res = list.stream()
                .flatMap(l -> list.stream().map(l2 -> new Pair(l,l2)))
                .filter(pair -> list.contains(pair.getRest()))
                .findFirst()
                .orElse(null);

        Long mdp = null;
        if (res != null) {

            //mdp = (res.l1 % 987654321) * (res.l2%987654321) * (res.);
        }

        System.out.println(res.l1 + " + " + res.l2 + " + " + res.getRest());
    }

    public static class Pair {

        Pair(long l1, long l2) {
            this.l1 = l1;
            this.l2 = l2;
        }

        long l1;
        long l2;
        long sum;
        long rest;

        public long getSum() {
            return l1 + l2;
        }

        public long getRest() {
            return 987654321 - getSum();
        }
    }

    public static class Connexion {

        public Connexion(Integer s1, Integer s2) {
            this.s1 = s1;
            this.s2 = s2;
        }

        public static Connexion of(Integer s1, Integer s2) {
            return new Connexion(s1, s2);
        }
        public Integer s1;
        public Integer s2;

        public boolean equals(final Object o) {
            if (o == this) return true;
            if (!(o instanceof Connexion)) return false;
            final Connexion other = (Connexion) o;
            if (!other.canEqual((Object) this)) return false;
            if (Math.min(this.s1, this.s2)!=Math.min(other.s1, other.s2)
                    || Math.max(this.s1, this.s2)!=Math.max(other.s1, other.s2)){
                return false;
            }
            return true;
        }

        protected boolean canEqual(final Object other) {
            return other instanceof Connexion;
        }

        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $s1 = Math.min(this.s1, this.s2);
            result = result * PRIME + ($s1 == null ? 43 : $s1.hashCode());
            final Object $s2 = Math.max(this.s1, this.s2);
            result = result * PRIME + ($s2 == null ? 43 : $s2.hashCode());
            return result;
        }
    }
}
