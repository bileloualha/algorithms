class Solution {
    /*
    Given a string s containing just the characters '(', ')', '{', '}', '[' and ']', determine if the input string is valid.
     */
    public boolean isValid(String s) {
        Stack stack = new Stack();

        for (int i = 0; i<s.length(); i++) {

            char c = s.charAt(i);

            switch(c) {

                case '(':
                    stack.push(')');
                    break;
                case '[':
                    stack.push(']');
                    break;
                case '{':
                    stack.push('}');
                    break;
                default:
                    if (!stack.empty() && c == (char)stack.peek()) {
                        stack.pop();
                    }
                    else {
                        return false;
                    }
            }
        }

        if (stack.empty()) {
            return true;
        }

        return false;
    }
}