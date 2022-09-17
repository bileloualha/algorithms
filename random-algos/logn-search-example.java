class Solution {
    /*
    Given a sorted array of distinct integers and a target value, return the index if the target is found. If not, return the index where it would be if it were inserted in order.
    Complexity is O(log(n))
     */
    public int searchInsert(int[] nums, int target) {
        int right = nums.length-1;
        int left = 0;
        //System.out.println(left + " " + right);
        while(!(left == right || left == right-1)) {

            if (nums[(right+left)/2] == target) {
                return (right+left)/2;
            }

            if (target<nums[(right+left)/2]) {
                right = (right+left)/2;
            }
            else {
                left = (right+left)/2;
            }

            //System.out.println(left + " " + right);
        }

        if (nums[right]<target) {return right+1;}
        else if (nums[left]< target) {return right;}
        else {return left;}
    }
}