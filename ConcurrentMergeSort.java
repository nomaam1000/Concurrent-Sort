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
    
    public static int[] merge(int[] leftArr, int[] rightArr){
        int[] mergedArr = new int[leftArr.length + rightArr.length];
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
        int[] rightResult = rightThread.compute();
        int[] leftResult = leftThread.join();

        return merge(leftResult, rightResult);
    }


    public static void main(String[] args){
        int[] arr = {37, 92, 14, 58, 6, 81, 23, 60, 3, 134, 6, 33};

        ForkJoinPool pool = new ForkJoinPool();

        pool.invoke(new ConcurrentMergeSort(arr));

        pool.close();

        for (int i : arr) {
            System.out.print(i + ", ");
        }
    }

}
