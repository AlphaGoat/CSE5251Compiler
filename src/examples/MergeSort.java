package examples;
class MergeSort {
    public static void main(String[] args) {
        System.out.println(new MS().Start(10));
    }
}

class MS {
    int[] array;
    int size;

    public int Start(int sz) {
        int aux01;
        aux01 = this.Init(sz);
        aux01 = this.Print();
        System.out.println(9999);
        aux01 = size-1;
        aux01 = this.sort(0, aux01);
        aux01 = this.Print();
        return 0;
    }

    public int sort(int beginning, int end) {
        int aux01;
        int mid;
        if (beginning < end) {
            mid = this.Div(beginning + end);
            aux01 = this.sort(beginning, mid);
            aux01 = this.sort(mid+1, end);
            aux01 = this.merge(beginning, mid, end);
        }
        else {mid = 0;}

        return 0;
    }

    public int merge(int beginning, int mid, int end) {
        int i;
        int j;
        int k;
        int index;
        int[] temp;
        i = beginning;
        j = mid + 1;
        index = beginning;

        temp = new int[10];

        while((i < (mid + 1)) && (j < (end + 1))) {
           if ((array[i]) < (array[j])) {
            temp[index] = array[i];
            i = i + 1;
           } 

           else {
               temp[index] = array[j];
               j = j+1;
           }

           index = index + 1;


        }

        if (mid < i) {

            while (j < (end+1)) {
                temp[index] = array[j];
                index = index + 1;
                j = j + 1;
            }
        }

        else {
            while(i < (mid + 1)) {
                temp[index] = array[i];
                index = index + 1;
                i = i + 1;
            }
        }

        k = beginning;
        while (k < index) {
            array[k] = temp[k];
            k = k + 1;
        }

        return 0;
    }

    public int Div(int num){
	int count01 ;
	int count02 ;
	int aux03 ;

	count01 = 0 ;
	count02 = 0 ;
	aux03 = num - 1 ;
	while (count02 < aux03) {
	    count01 = count01 + 1 ;
	    count02 = count02 + 2 ;
	}
	return count01 ;	
    }

    public int Print() {
        int j;
        j = 0;
        while (j < (size)) {
            System.out.println(array[j]);
            j = j + 1;
        }
        System.out.println(99999);
        return 0;
    }

    public int Init(int sz) {
        size = sz;
        array = new int[sz];

        array[0] = 30;
        array[1] = 5;
        array[2] = 13;
        array[3] = 79;
        array[4] = 6;
        array[5] = 24;
        array[6] = 101;
        array[7] = 2;
        array[8] = 0;
        array[9] = 2;

        return 0;
    }


}
