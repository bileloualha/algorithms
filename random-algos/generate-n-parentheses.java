class Solution {
    /*
    Given n pairs of parentheses, write a function to generate all combinations of well-formed parentheses.
     */

    public List<String> generateParenthesis(int n) {
        Set<String> tried = new HashSet<>();
        List<String> res = new ArrayList<>();
        dive("",2*n,tried,res);
        return res;
    }

    public void dive(String s, int n, Set<String> tried, List<String> res) {

        int k = isValid(s,n);

        if (!tried.contains(s) && k>=0) {

            if (n==s.length()) {
                tried.add(s);
                res.add(s);
            }

            else if (k==s.length()-n) {
                for (int i = 0;i<k;i++) {
                    s=s+")";
                }
                tried.add(s);
                res.add(s);
            }

            else {
                dive(s+")", n, tried, res);
                dive(s+"(", n, tried, res);
            }
        }
    }

    public int isValid(String s, int n) {
        int par = 0;

        for (int i=0;i<s.length();i++) {
            if (s.charAt(i) == '(') {par++;}
            if (s.charAt(i) == ')') {par--;}

            if (par<0) {return par;}
        }

        if (par>n-s.length()) {return -1;}

        return par;
    }
}