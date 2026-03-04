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
        // end of our (sub)array
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
            System.err.println("startCopy: " + startCopy + ", endCopy: " + endCopy);
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


    //Classes extending RecursiveTask<Integer> must implement compute() method
    protected Integer compute(){
        //base case
        if (start >= end){
            return null;
        }

        //we calculate our pivot index using our partition function
        int pivotIndex = quickSortPartition(start, end, arr);

        //we create 2 threads to concurrently sort the left and right subarrays
        ConcurrentQuickSort leftThread = new ConcurrentQuickSort(start, pivotIndex - 1, arr);
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

    public static void main(String[] args) {
        int[] arr = {37, 92, 14, 58, 6, 81, 23, 60, 3, 134, 6, 33};

        ForkJoinPool pool = new ForkJoinPool();

        pool.invoke(new ConcurrentQuickSort(0, arr.length-1, arr));
        
        pool.close();
        
        for (int i : arr) {
            System.out.print(i + ", ");
            
        }


    }
}