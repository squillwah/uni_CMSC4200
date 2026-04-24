#include <iostream>
#include <cstdlib>
#include <ctime>
//#define LISTSIZE 100000000
#define LISTSIZE 100000
// Make sure to unlimit your stack: ulimit -s unlimited
int main(void) {
    srand(time(0));
    int nums[LISTSIZE];
    for (int i = 0; i < LISTSIZE; i++)
        nums[i] = rand() % LISTSIZE * 10;

    std::cout << "Starting..." << std::endl;

    int left = 0;
    int right = LISTSIZE - 1;
    int lastSwap;
    bool sorted = false;

    while (!sorted) {
        sorted = true;

        // Forward pass: bubble the largest unsorted element to the right
        lastSwap = left;
        for (int i = left; i < right; i++) {
            if (nums[i] > nums[i + 1]) {
                int tmp = nums[i];
                nums[i] = nums[i + 1];
                nums[i + 1] = tmp;
                sorted = false;
                lastSwap = i;
            }
        }
        right = lastSwap; // Everything after lastSwap is already sorted

        if (sorted) break;

        // Backward pass: bubble the smallest unsorted element to the left
        lastSwap = right;
        for (int i = right; i > left; i--) {
            if (nums[i] < nums[i - 1]) {
                int tmp = nums[i];
                nums[i] = nums[i - 1];
                nums[i - 1] = tmp;
                lastSwap = i;
            }
        }
        left = lastSwap; // Everything before lastSwap is already sorted
    }

    std::cout << "Done!" << std::endl;
    return 0;
}