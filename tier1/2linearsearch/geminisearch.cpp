#include <iostream>
#include <cstdlib>
#include <ctime>
#include <algorithm> // Required for std::binary_search or std::lower_bound

#define LISTSIZE 100000

int main(void) {
    srand(time(0));

    // Optimization 1: Use the Heap
    // Stack allocation for 1 billion ints will likely SEGFAULT on most systems.
    int* nums = new int[LISTSIZE];

    nums[0] = rand() % 1000;
    for (int i = 1; i < LISTSIZE; i++) 
        nums[i] = nums[i-1] + rand() % 10;

    int findme = nums[rand() % LISTSIZE];

    // Optimization 2: Binary Search
    // Since the array is generated such that nums[i] >= nums[i-1], it is sorted.
    // Linear search is O(N). Binary search is O(log N).
    
    int* it = std::lower_bound(nums, nums + LISTSIZE, findme);
    int index = std::distance(nums, it);

    std::cout << "Found " << findme << " at index " << index << " in array." << std::endl;

    // Clean up heap memory
    delete[] nums;

    return 0;
}