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

    int findme = nums[rand() % LISTSIZE];

    int left = 0;
    int right = LISTSIZE - 1;
    int i = -1;

    while (left <= right) {
        int mid = left + (right - left) / 2;

        if (nums[mid] == findme) {
            i = mid;
            break;
        } else if (nums[mid] < findme) {
            left = mid + 1;
        } else {
            right = mid - 1;
        }
    }

    std::cout << "Found " << findme << " at index " << i << " in array." << std::endl;

    return 0;
}