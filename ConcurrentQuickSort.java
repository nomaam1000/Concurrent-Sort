import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class ConcurrentQuickSort extends RecursiveTask<Integer> {

    int start;
    int end;
    int[] arr; 

    //constructor for our quicksort thread class
    public ConcurrentQuickSort(int start, int end, int[] arr){
        this.arr = arr;
        this.start = start;
        this.end = end;
    }

    public int quickSortPartition(int start, int end, int[] arr){
        //generate a random pivot between the beginning and 
        //end of our (sub)array
        int startCopy = start;
        int endCopy = end; 
        int pivotIndex = new Random().nextInt(endCopy - startCopy) + startCopy;
        int pivot = arr[pivotIndex];

        //we swap our pivot with the last element of our (sub)array
        //this ensures that we don't have to continuously shift our elements
        //to ensure items on the left are less than the pivot and items
        //on the right are greater than the pivot
        int temp = arr[pivotIndex];
        arr[pivotIndex] = arr[endCopy];
        arr[endCopy] = temp;
        endCopy--;

        while (startCopy <= endCopy){
            //we increment our startCopy pointer until we find the first
            //element greater than our pivot
            //originally tried to use a for loop here but it was causing some issues with
            //the pointers and the swapping since the while loop would increment/decrement 
            //the pointers and the inner while loop would also increment/decrement the pointers
            if (arr[startCopy] < pivot){
                startCopy++;
                continue;
            }

            //we decrement our endCopy pointer until we find the first
            //element less than our pivot
            if (arr[endCopy] > pivot){
                endCopy--;
                continue;
            }

            //we swap our arr[startCopy] and arr[endCopy] elements
            //since arr[startCopy] is greater than our pivot and 
            //arr[endCopy] is less than our pivot
            temp = arr[startCopy];
            arr[startCopy] = arr[endCopy];
            arr[endCopy] = temp;

            //we increment/decrement our pointers
            startCopy++;
            endCopy--;
        }

        //we swap our pivot back to its correct position
        //which is to the right of the last element we found
        //that was less than it
        temp = arr[endCopy+1];
        arr[endCopy+1] = pivot;
        arr[end] = temp;

        //we return pivot index in order to facilitate recursive
        //calls on the left and right subarrays
        return endCopy + 1;
    }

    public int[] serialQuickSort(int start, int end, int[] arr2){
        //base case
        if (start >= end){
            return arr2;
        }

        //we calculate our pivot index using our partition function
        int pivotIndex = quickSortPartition(start, end, arr2);

        //we recursively call our serial quicksort on the left and right subarrays
        serialQuickSort(start, pivotIndex - 1, arr2);
        serialQuickSort(pivotIndex+1, end, arr2);

        return arr2;
    }


    //Classes extending RecursiveTask<Integer> must implement compute() method
    protected Integer compute(){
        //base case
        if (start >= end){
            return null;
        }

        //we calculate our pivot index using our partition function
        int pivotIndex = quickSortPartition(start, end, arr);

        //we create 2 threads to concurrently sort the left and right subarrays
        ConcurrentQuickSort leftThread = new ConcurrentQuickSort(this.start, pivotIndex - 1, arr);
        ConcurrentQuickSort rightThread = new ConcurrentQuickSort(pivotIndex+1, end, arr);

        //the left thread is a forked thread, meaning it will run concurrently
        //and will "fork"
        leftThread.fork();
        //the right thread is the main thread, meaning it will run and wait for
        //the left to "join"
        rightThread.compute();
        leftThread.join();

        return null;
    }

    public static int[] createRandomArr(int size){
        int[] randArr = new int[size];

        for (int i = 0; i < size; i++){
            randArr[i] = new Random().nextInt(10000);
        }

        return randArr;
    }

    public static void main(String[] args) {
        int arraySize = 10000;
        for (int i = 0; i < 12; i++) {
            
            int[] arr = createRandomArr(arraySize*(int)Math.pow(2, i));
            int[] arr2 = createRandomArr(arraySize*(int)Math.pow(2, i));
            
            ForkJoinPool pool = new ForkJoinPool(3);
            long startTime = System.nanoTime();
            pool.invoke(new ConcurrentQuickSort(0, arr.length-1, arr));
            long endTime = System.nanoTime();
            pool.close();
            long durationConcurrent = endTime - startTime;
            

            startTime = System.nanoTime();
            arr2 = new ConcurrentQuickSort(0, arr2.length-1, arr2).serialQuickSort(0, arr2.length-1, arr2);
            endTime = System.nanoTime();
            long durationSerial = endTime - startTime;

            System.out.println("Array Size: " + arr.length);
            System.out.println("Concurrent Quick Sort Time; " + durationConcurrent + " nanoseconds");
            System.out.println("Serial Quick Sort Time: " + durationSerial + " nanoseconds");

            if (durationConcurrent < durationSerial){
                System.out.println("Concurrent Quick Sort was faster by " + (durationSerial - durationConcurrent) + " nanoseconds");
            }
            else{
                System.out.println("Serial Quick Sort was faster by " + (durationConcurrent - durationSerial) + " nanoseconds");
            }
            System.out.println();
        }
    }
}