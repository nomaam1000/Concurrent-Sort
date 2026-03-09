import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class ConcurrentMergeSort extends RecursiveTask<int[]>{
    int[] arr;

    //constructor for our merge sort thread class
    public ConcurrentMergeSort(int arr[]){
        this.arr = arr;
    }
    
    public static int[] merge(int[] leftArr, int[] rightArr, int[] arr){
        int[] mergedArr = new int[arr.length];
        int index = 0;
        int pointerLeft = 0;
        int pointerRight = 0;

        //we sort the left and right arrays until we have sorted all of
        //our elements in one of our subarrays
        while(pointerLeft < leftArr.length && pointerRight < rightArr.length){
            if(leftArr[pointerLeft] < rightArr[pointerRight]){
                mergedArr[index] = leftArr[pointerLeft];
                pointerLeft++;
            }
            else{
                mergedArr[index] = rightArr[pointerRight];
                pointerRight++;
            }
            index++;
        }

        //we check if we have finished sorting the left subarray, if we have,
        //then we add the remaining elements from the right subarray
        //otherwise, we do the opposite for the left subarray
        boolean leftArrFinished = pointerLeft >= leftArr.length;
        if(leftArrFinished){
            for(int i = pointerRight; i < rightArr.length; i++){
                mergedArr[index] = rightArr[i];
                index++;
             }
          }
        else{
            for(int i = pointerLeft; i < leftArr.length; i++){
                mergedArr[index] = leftArr[i];
                index++;
           }
        }
        return mergedArr;
    }

    public int[] serialMergeSort(int[] arr2){
        //base case
        if (arr2.length <= 1){
            return arr2;
        }
        
        //we calculate the midpoint of our (sub)array and create 2 subarrays
        int mid = arr2.length/2;
        
        //we create 2 subarrays using our midpoint
        int[] leftArr = Arrays.copyOfRange(arr2, 0, mid);
        int[] rightArr = Arrays.copyOfRange(arr2, mid, arr2.length);

        //we recursively call merge sort on the left and right subarrays
        leftArr = serialMergeSort(leftArr);
        rightArr = serialMergeSort(rightArr);

        return merge(leftArr, rightArr, arr2);
    }

    @Override
    public int[] compute(){
        //base case
        if (arr.length <= 1){
            return arr;
        }
        
        //we calculate the midpoint of our (sub)array and create 2 subarrays
        int mid = arr.length/2;
        
        //we create 2 subarrays using our midpoint
        int[] leftArr = Arrays.copyOfRange(arr, 0, mid);
        int[] rightArr = Arrays.copyOfRange(arr, mid, arr.length);

        //we create 2 threads to conccurently split/sort our left and right subarrays
        ConcurrentMergeSort leftThread = new ConcurrentMergeSort(leftArr);
        ConcurrentMergeSort rightThread = new ConcurrentMergeSort(rightArr);

        //the left thread is a forked thread, meaning it will run concurrently
        //and will "fork"
        leftThread.fork();
        //the right thread is n the main thread meaning it will run and wait for
        //the left to "join"
        rightThread.compute();
        leftThread.join();

        arr = merge(leftThread.arr, rightThread.arr, arr);

        return arr;
    }

    public static int[] createRandomArr(int size){
        int[] randArr = new int[size];

        for (int i = 0; i < size; i++){
            randArr[i] = new Random().nextInt(1000);
        }

        return randArr;
    }


    public static void main(String[] args){
        int arraySize = 1000;
        for (int i = 1; i < 15; i++) {
            int[] arr = createRandomArr(arraySize*(int)Math.pow(2, i));
            int[] arr2 = arr;
            
            ForkJoinPool pool = new ForkJoinPool(10);
            long startTime = System.nanoTime();
            arr = pool.invoke(new ConcurrentMergeSort(arr));
            long endTime = System.nanoTime();
            pool.close();
            long durationConcurrent = endTime - startTime;


            startTime = System.nanoTime();
            arr2 = new ConcurrentMergeSort(arr2).serialMergeSort(arr2);
            endTime = System.nanoTime();
            long durationSerial = endTime - startTime;

            System.out.println("Array Size: " + arr.length);
            System.out.println("Concurrent Merge Sort Time; " + durationConcurrent + " nanoseconds");
            System.out.println("Serial Merge Sort Time: " + durationSerial + " nanoseconds");

            if (durationConcurrent < durationSerial){
                System.out.println("Concurrent Merge Sort was faster by " + (durationSerial - durationConcurrent) + " nanoseconds");
             }
            else{
                System.out.println("Serial Merge Sort was faster by " + (durationConcurrent - durationSerial) + " nanoseconds");
            }
            System.out.println();
        }
        
    }
}
