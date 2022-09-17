class Solution {
    /*
    Given an unsorted integer array nums, return the smallest missing positive integer.

    O(n) time complexity and O(1) space complexity

     */
    public int firstMissingPositive(int[] nums) {

        eliminatezeros(nums);

        for (int i=0;i<nums.length;i++) {
            deepdive(nums,nums[i]);
        }

        for (int i=0;i<nums.length;i++) {
            if (nums[i] != 0) {return i+1;}
        }

        return nums.length+1;
    }

    void eliminatezeros(int[] nums) {
        for (int i=0; i<nums.length;i++) {
            if (nums[i] == 0) {nums[i] = -1;}
        }
    }


    void deepdive(int[] nums,int x) {

        if (x>0 && x<=nums.length) {
            int tmp = nums[x-1];
            nums[x-1] = 0;
            deepdive(nums,tmp);
        }
    }
}