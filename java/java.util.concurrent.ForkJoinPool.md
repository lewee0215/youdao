# Fork/Join框架
https://blog.csdn.net/tyrroo/article/details/81390202  

```java
// ForkJoinPool构造函数
public ForkJoinPool(int parallelism,
                        ForkJoinWorkerThreadFactory factory,
                        UncaughtExceptionHandler handler,
                        boolean asyncMode)

/**
parallelism：可并行级别，Fork/Join框架将依据这个并行级别的设定，决定框架内并行执行的线程数量

factory：当Fork/Join框架创建一个新的线程时，同样会用到线程创建工厂

handler：异常捕获处理器。当执行的任务中出现异常，并从任务中被抛出时，就会被handler捕获

asyncMode: Fork/Join框架中为每一个独立工作的线程准备了对应的待执行任务队列，这个任务队列是使用数组进行组合的双向队列。即是说存在于队列中的待执行任务，即可以使用先进先出的工作模式，也可以使用后进先出的工作模式
*/
```
![](https://img-blog.csdn.net/20170512085043577?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQveWlud2Vuamll/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

当asyncMode设置为ture的时候，队列采用先进先出方式工作；反之则是采用后进先出的方式工作，该值默认为false

## fork方法和join方法
每一个ForkJoinWorkerThread线程都具有一个独立的任务等待队列（work queue），这个任务队列用于存储在本线程中被拆分的若干子任务

## fork/join - 归并算法
![](https://img-blog.csdn.net/20170514100831229?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQveWlud2Vuamll/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

```java
......
/**
 * 使用Fork/Join框架的归并排序算法
 * @author yinwenjie
 */
public class Merge2 {
 
    private static int MAX = 100000000;
 
    private static int inits[] = new int[MAX];
 
    // 同样进行随机队列初始化，这里就不再赘述了
    static {
        ......
    }
 
    public static void main(String[] args) throws Exception {   
        // 正式开始
        long beginTime = System.currentTimeMillis();
        ForkJoinPool pool = new ForkJoinPool();
        MyTask task = new MyTask(inits);
        ForkJoinTask<int[]> taskResult = pool.submit(task);
        try {
            taskResult.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace(System.out);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("耗时=" + (endTime - beginTime));      
    }
 
    /**
     * 单个排序的子任务
     * @author yinwenjie
     */
    static class MyTask extends RecursiveTask<int[]> {
 
        private int source[];
 
        public MyTask(int source[]) {
            this.source = source;
        }
 
        /* (non-Javadoc)
         * @see java.util.concurrent.RecursiveTask#compute()
         */
        @Override
        protected int[] compute() {
            int sourceLen = source.length;
            // 如果条件成立，说明任务中要进行排序的集合还不够小
            if(sourceLen > 2) {
                int midIndex = sourceLen / 2;
                // 拆分成两个子任务
                MyTask task1 = new MyTask(Arrays.copyOf(source, midIndex));
                task1.fork();
                MyTask task2 = new MyTask(Arrays.copyOfRange(source, midIndex , sourceLen));
                task2.fork();
                // 将两个有序的数组，合并成一个有序的数组
                int result1[] = task1.join();
                int result2[] = task2.join();
                int mer[] = joinInts(result1 , result2);
                return mer;
            } 
            // 否则说明集合中只有一个或者两个元素，可以进行这两个元素的比较排序了
            else {
                // 如果条件成立，说明数组中只有一个元素，或者是数组中的元素都已经排列好位置了
                if(sourceLen == 1
                    || source[0] <= source[1]) {
                    return source;
                } else {
                    int targetp[] = new int[sourceLen];
                    targetp[0] = source[1];
                    targetp[1] = source[0];
                    return targetp;
                }
            }
        }
 
        private static int[] joinInts(int array1[] , int array2[]) {
            int destInts[] = new int[array1.length + array2.length];
            int array1Len = array1.length;
            int array2Len = array2.length;
            int destLen = destInts.length;
    
            // 只需要以新的集合destInts的长度为标准，遍历一次即可
            for(int index = 0 , array1Index = 0 , array2Index = 0 ; index < destLen ; index++) {
                int value1 = array1Index >= array1Len?Integer.MAX_VALUE:array1[array1Index];
                int value2 = array2Index >= array2Len?Integer.MAX_VALUE:array2[array2Index];
                // 如果条件成立，说明应该取数组array1中的值
                if(value1 < value2) {
                    array1Index++;
                    destInts[index] = value1;
                }
                // 否则取数组array2中的值
                else {
                    array2Index++;
                    destInts[index] = value2;
                }
            }
    
            return destInts;
        }
    }
}
```
