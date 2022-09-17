class Solution {

    /*

    Given a string s, return the longest palindromic substring in s.
    s is a palindrome if reverse(s) = s

     */

    private Map<String, Boolean> isPrimitiveMap = new HashMap<>();

    public String longestPalindrome(String s) {

        if (s.isEmpty()) {return "";}

        if (s.length() == 1) {return s;}

        String res = "";

        for (int i=0; i<s.length(); i++) {


            if (isPrimitive(s.substring(i-res.length(),i+1))) {
                res = s.substring(i-res.length(),i+1);
            }

            if (i-res.length() > 0 && isPrimitive(s.substring(i-res.length()-1,i+1))) {
                res = s.substring(i-res.length()-1,i+1);
            }
        }
        return res;
    }


    private Boolean isPrimitive(String s) {


        if (isPrimitiveMap.containsKey(s)) {
            return isPrimitiveMap.get(s);
        }

        if (s.length() == 1) {
            isPrimitiveMap.put(s,true);
            return true;
        }

        int i=0;
        int j=s.length()-1;

        while (i<s.length()/2 && j>=s.length()/2) {

            if (s.charAt(i) != s.charAt(j)) {
                isPrimitiveMap.put(s,false);
                return false;
            }
            i++;
            j--;
        }

        isPrimitiveMap.put(s,true);
        return true;
    }
}