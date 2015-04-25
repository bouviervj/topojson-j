package json.algorithm;

public class Jenks {


	public static double[] computeJenks(int iNClass,double[] iSerie){
		
		if (iNClass>iSerie.length) {
			double[] kclass = new double[iNClass+1];
			for (int i=0; i<iSerie.length-1; i++){
				kclass[i*2]=iSerie[i];
				kclass[i*2+1]=iSerie[i+1];
			}
			return kclass;
		}

		double[][] mat1 = new double[iSerie.length+1][];
		for (int x = 0, xl = iSerie.length + 1; x < xl; x++) {
			mat1[x] = new double[iNClass + 1];
		}

		double[][] mat2 = new double[iSerie.length+1][];
		for (int x = 0, xl = iSerie.length + 1; x < xl; x++) {
			mat2[x] = new double[iNClass + 1];
		}

		for (int y = 1, yl = iNClass + 1; y < yl; y++) {
			mat1[0][y] = 1;
			mat2[0][y] = 0;
			for (int t = 1, tl = iSerie.length + 1; t < tl; t++) {
				mat2[t][y] = Double.POSITIVE_INFINITY;
			}
		}

		double v = 0.0;
		for ( int l = 2, ll = iSerie.length + 1; l < ll; l++) {
			double s1 = 0.0;
			double s2 = 0.0;
			double w = 0.0;
			for ( int m = 1, ml = l + 1; m < ml; m++) {
				int i3 = l - m + 1;
				double val = iSerie[i3 - 1];
				s2 += val * val;
				s1 += val;
				w += 1.0;
				v = s2 - (s1 * s1) / w;
				int i4 = i3 - 1;
				if (i4 != 0) {
					for ( int p = 2, pl = iNClass + 1; p < pl; p++) {
						if (mat2[l][p] >= (v + mat2[i4][p - 1])) {
							mat1[l][p] = i3;
							mat2[l][p] = v + mat2[i4][p - 1];
						}
					}
				}
			}
			mat1[l][1] = 1;
			mat2[l][1] = v;
		}

		int k = iSerie.length;
		double[] kclass = new double[iNClass+1];
		kclass[iNClass]=iSerie[iSerie.length-1];
		kclass[0]=iSerie[0];

		int countNum = iNClass;
		while (countNum >= 2) {
			int id = (int) mat1[k][countNum] - 2;
			kclass[countNum - 1] = iSerie[id];
			k = (int) mat1[k][countNum] - 1;
			countNum -= 1;
		}
		
		if (kclass[0] == kclass[1]) {
			kclass[0] = 0;
		}

		return kclass	;

	}

}
