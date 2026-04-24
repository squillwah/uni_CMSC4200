#include <iostream>
#include <cstdlib>
#include <ctime>
#define LISTSIZE 100000
// Make sure to unlimit your stack: ulimit -s unlimited
int main(void) {
    srand(time(0));
    int nums[LISTSIZE];
    nums[0] = rand() % 1000;
    for (int i = 1; i < LISTSIZE; i++) 
        nums[i] = nums[i-1] + rand() % 10;

    int findme = nums[rand() % LISTSIZE]; // Fix: % not &

    // Binary search — valid because nums is non-decreasing
    int lo = 0, hi = LISTSIZE - 1, i = -1;
    while (lo <= hi) {
        int mid = lo + (hi - lo) / 2; // Avoids overflow vs (lo+hi)/2
        if (nums[mid] == findme) {
            i = mid;
            break;
        } else if (nums[mid] < findme) {
            lo = mid + 1;
        } else {
            hi = mid - 1;
        }
    }

    std::cout << "Found " << findme << " at index " << i << " in array." << std::endl;
    return 0;
}