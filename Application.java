package controllers;
import play.*;
import play.db.jpa.JPABase;
import play.mvc.*;

//import java.security.Provider.Service;
import java.util.*;

import javax.validation.Valid;

import org.h2.command.dml.Delete;
import org.h2.command.dml.Select;
import org.hibernate.ejb.criteria.expression.function.SqrtFunction;
import org.hibernate.validator.cfg.defs.MaxDef;

import com.sun.mail.imap.protocol.Item;
import com.sun.xml.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import net.sf.ehcache.search.aggregator.Count;
import net.sf.ehcache.search.aggregator.Max;
import models.DerajatGejala;
import models.DetailDiagnosis;
import models.Kasus;
import models.KasusUji;
import models.Som;

import models.*;

import java.util.Random;

public class Application extends Controller {

public static KelasSom jumlahkelas = KelasSom.findById((long)1);
public List<Long> listderajat = new ArrayList<Long>();

public static Login user = null;

    public static void index() {
    	String userid = " ";
    	if(user!=null){
    		userid =user.level.id.toString();
    	}
        render(userid);
    }
    
    public static void training(){
 	   String userid = " ";
    	if(user!=null){
    		userid =user.level.id.toString();
    	}
 	   List listkasus = Kasus.findAll();
 	   List listdetailkasus = DetailKasus.findAll();
 	   render(listkasus, listdetailkasus, userid);
    }
    public static void datanormalisasi(){
    	String userid = " ";
    	if(user!=null){
    		userid =user.level.id.toString();
    	}
    	List listnormalisasi =Normalisasi.findAll();
     	
     	render(userid, listnormalisasi);
    }
    
    public static void normalisasi(){
    	String userid = " ";
     	if(user!=null){
     		userid =user.level.id.toString();
     	}
     	Normalisasi.deleteAll();
     	double normalisasi;
     	double mingejala = 0;
    	double maxgejala = 3;
     	int jumlahgejala = (int) Gejala.count(); 
     
     	List listkasus = Kasus.findAll();
		Iterator kasus = listkasus.iterator();
		while (kasus.hasNext()){
			Kasus k = (Kasus) kasus.next();
			System.out.println("kasus ke - " + k.kode_kasus);
			for (int i=0;i<jumlahgejala;i++){
	    			Gejala gejala = Gejala.findById((long)(i+1));
	    			DetailKasus detailkasus = DetailKasus.find("kasus=? and gejala=?", k, gejala).first();
	    			normalisasi = (double)((detailkasus.derajat.nilai - mingejala)/(maxgejala-mingejala));
	    			System.out.println("Gejala ke - " + gejala.kode_gejala);
	    			System.out.println("Nilai gejala = " + normalisasi);
	    			
	    			Normalisasi norm = new Normalisasi();
	    			norm.kasus = k;
					norm.derajat = normalisasi;
	    			norm.gejala = gejala;
	    			norm.save();
	    			
	    	}
		}		
		List listnormalisasi =Normalisasi.findAll();
     	
     	render(userid, listnormalisasi);
    }

    
    public static void training1(long id, double learningRate, int jumlahClass, double penurunanPembelajaran, int epoch){    
    	String userid = " ";
    	if(user!=null){
    		userid =user.level.id.toString();
    	}
    	Som.deleteAll();
    	jumlahkelas = KelasSom.findById((long)1);
    	jumlahkelas.jumlahKelas = jumlahClass;
    	jumlahkelas.save();
    	double learningrate = learningRate;
    	int jumlahgejala = (int) Gejala.count(); 
    	
    	for (int i=1;i<=jumlahgejala;i++){
    		for (int j=1;j<=jumlahkelas.jumlahKelas; j++){
    			Random n = new Random();
        		double r = n.nextDouble(); //random angka 0-1   
        		Som bbt = new Som();
        		bbt.save();
        		Gejala gejala = Gejala.findById((long) i);
        		bbt.gejala = gejala;
        		bbt.kelas = j;
        		bbt.bobot =  r;
        		bbt.save();
    		}
    	}    	
    	int epochh = 0;
    	System.out.println("Maksimum Epoh - " + epoch);	

    	while (epochh <= epoch){
    		//persiapan penyimpanan
			double nilaiminimum = 100; 
			int kelasterpilih = 0;			
			
			List listkasus = Kasus.findAll();
			Iterator kasus = listkasus.iterator();
			while (kasus.hasNext()){
				Kasus k = (Kasus) kasus.next();
				double [] [] matrikseudlia = new double [jumlahgejala] [jumlahkelas.jumlahKelas];
				ArrayList<Double> matriksmin = new ArrayList<Double>();
				for (int j=0;j<jumlahkelas.jumlahKelas; j++){
					matriksmin.add(0.0);
				}
				for (int i=0;i<jumlahgejala;i++){
		    		for (int j=0;j<jumlahkelas.jumlahKelas; j++){
		    			Gejala gejala = Gejala.findById((long)(i+1));
		    			Som bbt = Som.find("gejala=? and kelas=?", gejala,(j+1)).first(); 
		    			DetailKasus detailkasus = DetailKasus.find("kasus=? and gejala=?", k, gejala).first();
		    			matrikseudlia[i][j] = Math.pow((bbt.bobot - detailkasus.derajat.nilai),2);
		    			matriksmin.set(j, matriksmin.get(j) + matrikseudlia[i][j]) ;		    			
		    		}		    		
		    	}
				for (int j=0;j<jumlahkelas.jumlahKelas; j++){		    		
					if (nilaiminimum > matriksmin.get(j)){	
	    				nilaiminimum = matriksmin.get(j);	    				
	    				kelasterpilih = j+1;	    				
	    				}					
		    		}		
				
				//update bobot kelas terpilih
				for (int i=1;i<=jumlahgejala;i++){
					Gejala gejala = Gejala.findById((long)i);
						Som bbt = Som.find("gejala=? and kelas=?",gejala , kelasterpilih).first();
						DetailKasus detailkasus = DetailKasus.find("kasus=? and gejala=?", k, gejala).first();
						bbt.bobot = bbt.bobot + (learningrate*(detailkasus.derajat.nilai - bbt.bobot));	
						bbt.save();
		    	}				
				k.kelas = kelasterpilih;
				k.save();
				nilaiminimum = 100;
			}			
		//perbaiki learning rate
		learningrate = (double)(learningrate * penurunanPembelajaran);
		epochh = epochh +1;
		System.out.println("learningrate = " + learningrate);
		System.out.println("Epoch Perulangan - " + epochh);	
    	}
    	List listbobot = Som.findAll();
    	render(listbobot, userid);   	
}
    
    public static void bobotsom(){
    	String userid = " ";
    	if(user!=null){
    		userid =user.level.id.toString();
    	}
    	List listbobot= Som.findAll();
    	render(listbobot,userid);
    }
    
    
    public static void datauser(){
    	String userid = " ";
    	if(user!=null){
    		userid =user.level.id.toString();
    	}
    	List listdatauser= Login.findAll();
    	render(listdatauser,userid);
    }
    
    public static void logout(){
    	
    	user = null;
    	index();
    }
    public static void edituser(long id){
    	List listlevel = Level.findAll();
    	String userid = " ";
    	if(user!=null){
    		userid =user.level.id.toString();
    	}
    	Login objek;
    	objek = Login.find("id=?", id).first();
    	if
    	(objek == null){
    		objek = new Login();
    	}
    	render (objek, userid, listlevel);
    }
    public static void simpanuser(@Valid Login objek){
    	Login x;
    	x = objek;
    	if(validation.hasErrors()){
    		params.flash();
    		validation.keep();
    		edituser(x.id);
    	}
    	x.save();
    	datauser();
    }
    public static void hapususer(long id){
    	Login.delete("id=?", id);
    	datauser();
    }
    public static void login() {
    	String userid = " ";
    	if(user!=null){
    		userid =user.level.id.toString();
    	}
    	List listlevel = Level.findAll();
    	Level level = new Level();
    	String username="";
    	String password="";
        render(listlevel, username,password,level,userid);
    }
    
    public static void proseslogin(Level level,String username, String password) {
    	Login usernya = Login.find("level=? and username=? and password=?", level, username, password).first();
    	user = usernya;
    	System.out.println("conaaaaaaaaaaaa = " + user);
    	if(usernya!=null){
    		index();
    	}else{
    		login();
    	}
    }
    
    public static void tambahdiagnosis(long id) {
    	String userid = " ";
    	if(user!=null){
    		userid =user.level.id.toString();
    	}
    	
    	Diagnosis objek;
    	objek = Diagnosis.find("id=?", id).first();
    	if
    	(objek == null){
    		objek = new Diagnosis();
    	}
    	   
    	 render (objek, userid);
    	   }
    
    
    
    public static void diagnosis(@Valid Diagnosis objek) {
    	String userid = " ";
    	if(user!=null){
    		userid =user.level.id.toString();
    	}
    	Diagnosis x;
    	x = objek;
    	if(validation.hasErrors()){
    		params.flash();
    		validation.keep();
    		tambahdiagnosis(x.id);
    	}
    	x.save();
    	long id = x.id;
    	List listderajat = DerajatGejala.findAll();
       	int jmlhgejala = (int) Gejala.count();
       	ArrayList<DetailDiagnosis> listdetailkasus = new ArrayList<DetailDiagnosis>();
       	for(int i=0;i<jmlhgejala;i++){
       		DetailDiagnosis newdetailkasus = new DetailDiagnosis();
       		newdetailkasus.gejala = Gejala.findById((long) i+1);
       		newdetailkasus.diagnosis = Diagnosis.findById(x.id);
       		listdetailkasus.add(newdetailkasus);
       	}       
    	 render (listderajat, objek,id, listdetailkasus, userid);
    	   }
    
    
    public static void simpandata(ArrayList gejala, ArrayList derajat, Long kasus){
    	for(int i=0; i<gejala.size(); i++){
		   DetailDiagnosis dDiagnosis = new DetailDiagnosis();
		   DerajatGejala derajat_tmp = DerajatGejala.findById(Long.valueOf((String) derajat.get(i)));
		   Gejala gejala_tmp = Gejala.findById(Long.valueOf((String) gejala.get(i)));
		   Diagnosis diagnosis_tmp = Diagnosis.findById(kasus);
		   
		   dDiagnosis.derajat = derajat_tmp;
		   dDiagnosis.gejala = gejala_tmp;
		   dDiagnosis.diagnosis = diagnosis_tmp;
		   dDiagnosis.save();
	   }
    	pilihandiagnosis(kasus);
    	   }
    
    public static void pilihandiagnosis(long id) {
    	String userid = " ";
    	if(user!=null){
    		userid =user.level.id.toString();
    	}
    	
    render (userid, id);
    	
    }
    public static void doindexnn(long id){
    	Resultindex.deleteAll();
    	String userid = " ";
    	if(user!=null){
    		userid =user.level.id.toString();
    	}
    	
    	long start;
    	long end;
    	
    	start = System.currentTimeMillis();

    	//parameter
    	int maxGejala = 3;
    	double bobot = 34;
    	int maxusia = 0;
    	int minusia = 100;
    	double nilaiminimum = 100;

    	int jumlahgejala = (int) Gejala.count();
    	
    	List listkasuss= Kasus.findAll();
    	Iterator kasus = listkasuss.iterator();
    	while (kasus.hasNext()){
    		Kasus k = (Kasus) kasus.next();
    		if(maxusia<k.usia){
    			maxusia = k.usia;
    		}
    		if(minusia>k.usia){
    			minusia = k.usia;    			
    		}
    	}
    	//indexing
    	Diagnosis diagnosis = Diagnosis.findById(id);
    	ArrayList<Double> matriksmin = new ArrayList<Double>();
    	for (int j=0;j<jumlahkelas.jumlahKelas; j++){
			matriksmin.add(0.0);
		}
    	
    	double euclidian = 0.0;		
		for (int i=0;i<jumlahgejala;i++){
    		for (int j=0;j<jumlahkelas.jumlahKelas; j++){
    			Gejala gejala = Gejala.findById((long)(i+1));
    			Som bobotsom = Som.find("gejala=? and kelas=?", gejala, (j+1)).first();
    			System.out.println("bobot som = " + bobotsom);
    			DetailDiagnosis detdiagnosis = DetailDiagnosis.find("diagnosis=? and gejala=?", diagnosis, gejala).first();
    			euclidian = Math.pow((bobotsom.bobot- detdiagnosis.derajat.nilai),2);
    			matriksmin.set(j, matriksmin.get(j)+euclidian);
    			System.out.println("id diagnosis  = " + id);
    		}
    	}
		
		int kelasterpilih = 0;
		for (int j=0;j<jumlahkelas.jumlahKelas; j++){
			if (nilaiminimum>matriksmin.get(j)){			
				nilaiminimum = matriksmin.get(j);
				kelasterpilih = j+1;
				System.out.println("kelas terpilih = " + kelasterpilih);
			}
		}
			
		//retrieve
    	List listkasus = Kasus.find("kelas=?", kelasterpilih).fetch();    	
    	Iterator kas = listkasus.iterator();    	
    	while (kas.hasNext()){
    		Kasus k = (Kasus) kas.next();
    		
    		double simlokalusia = simlokalusia(maxusia, minusia, k.usia, diagnosis.usia,1);
    		double simlokalgejala = 0;
    		
    		double jst = 1; //+1 usia 
    		double jt = 1; //+1 usia
    		System.out.println("id diagnosis = " + diagnosis.id);
    		
    		List listgejala = Gejala.findAll();
    		Iterator gejala = listgejala.iterator();
    		while (gejala.hasNext()){
    			Gejala g = (Gejala) gejala.next();
    			DetailKasus detailkasus =DetailKasus.find("kasus=? and gejala=?" , k , g).first();
    			DetailDiagnosis detaildiagnosis =DetailDiagnosis.find("diagnosis=? and gejala=?" , diagnosis, g).first();
    			simlokalgejala = simlokalgejala + simlokalgejala(maxGejala,detailkasus.derajat.nilai , detaildiagnosis.derajat.nilai,1);
    			
    			if (detaildiagnosis.derajat.nilai!=0){
	   				jt = jt +1 ;
    			}
	   			if (detaildiagnosis.derajat.nilai!=0 && detailkasus.derajat.nilai!=0){
	   				jst = jst +1;
	   			}	   				
    		}    		
    		System.out.println("J(T) = " + jt);
        	System.out.println("J(S,T) = " + jst);
    		double hasilnearest = (double) (((simlokalgejala + simlokalusia)/bobot)*(jst/jt))*100;
    		
    		Resultindex rn = new Resultindex();
    		rn.kasus = k;
    		rn.diagnosis = diagnosis;
    		rn.hasilnearest = hasilnearest;
    		rn.save();
    		
    		jt = 1;
    		jst= 1;
    		
    	} 
    	//reuse
    	double maxSimNN = 0;
    	Resultindex hasilNN = null;
    	List listresulinx = Resultindex.findAll();
    	Iterator ridx = listresulinx.iterator();
    	while (ridx.hasNext()){
    		Resultindex rI = (Resultindex) ridx.next();
    		//Max Nearest Neighbor
    		if(maxSimNN<rI.hasilnearest){
    			maxSimNN = rI.hasilnearest;
    			hasilNN = rI;
    		}
    	}
    	end = System.currentTimeMillis();
    	double waktu;
    	waktu =  (double) ((end - start) / 1000.0);
    	
        System.out.println("\nWaktu yang diperlukan selama proses adalah " + ((end - start) / 1000.0) + " detik");
        System.out.println("maksimum usia = " + maxusia);
        System.out.println("minimum usia = " + minusia);
        
        //revisi
        if (maxSimNN < 80){
    	   Revisi revisi = new Revisi();
           revisi.nama_pasien = diagnosis.nama_pasien;
           revisi.usia = diagnosis.usia;
           revisi.nearestneighbor = maxSimNN;
           revisi.id_diagnosis = diagnosis.id;
           revisi.save();
           
        List listgejala = Gejala.findAll();
   		Iterator gejala = listgejala.iterator();
   		while (gejala.hasNext()){
   			Gejala g = (Gejala) gejala.next(); 
   			DetailDiagnosis detaildiagnosis =DetailDiagnosis.find("diagnosis=? and gejala=?" , diagnosis, g).first();
   			DetailRevisi detailRevisi = new DetailRevisi();
   			detailRevisi.gejala = detaildiagnosis.gejala;
   			detailRevisi.derajat = detaildiagnosis.derajat;
   			detailRevisi.revisi = Revisi.findById(revisi.id);
   			detailRevisi.save();
   		}
       }
        //retain
        if (maxSimNN != 100){
        	Kasus k = new Kasus();
        	k.usia = diagnosis.usia;
        	k.penyakit = hasilNN.kasus.penyakit;
        	k.save();
        	
        	 List listgejala = Gejala.findAll();
        		Iterator gejala = listgejala.iterator();
        		while (gejala.hasNext()){
        			Gejala g = (Gejala) gejala.next(); 
        			DetailDiagnosis detaildiagnosis =DetailDiagnosis.find("diagnosis=? and gejala=?" , diagnosis, g).first();
        			DetailKasus detailKasus = new DetailKasus();
        			detailKasus.gejala = detaildiagnosis.gejala;
        			detailKasus.derajat = detaildiagnosis.derajat;
        			detailKasus.kasus = Kasus.findById(k.id);
        			detailKasus.save();
        		} 
        }
		System.out.println("maxnya NN:" + maxSimNN);
    	List listindex = Resultindex.findAll();  	   	  	
       render (listindex,userid,maxSimNN, hasilNN,waktu);  
    }
    
    public static void doindexsmc(long id) {
    	Resultindex.deleteAll();
    	String userid = " ";
    	if(user!=null){
    		userid =user.level.id.toString();
    	}
    	
    	long start;
    	long end;
    	
    	start = System.currentTimeMillis();

    	
    	//parameter
    	double bobot = 34;
    	int maxusia = 0;
    	int minusia = 100;
    	double nilaiminimum = 100;

    	int jumlahgejala = (int) Gejala.count();
    	
    	List listkasuss= Kasus.findAll();
    	Iterator kasus = listkasuss.iterator();
    	while (kasus.hasNext()){
    		Kasus k = (Kasus) kasus.next();
    		if(maxusia<k.usia){
    			maxusia = k.usia;
    		}
    		if(minusia>k.usia){
    			minusia = k.usia;    			
    		}
    	}
    	Diagnosis diagnosis = Diagnosis.findById(id);
    	ArrayList<Double> matriksmin = new ArrayList<Double>();
    	for (int j=0;j<jumlahkelas.jumlahKelas; j++){
			matriksmin.add(0.0);
		}
    	
    	double euclidian = 0.0;		
		for (int i=0;i<jumlahgejala;i++){
    		for (int j=0;j<jumlahkelas.jumlahKelas; j++){
    			Gejala gejala = Gejala.findById((long)(i+1));
    			Som bobotsom = Som.find("gejala=? and kelas=?", gejala, (j+1)).first();
    			System.out.println("bobot som = " + bobotsom);
    			DetailDiagnosis detdiagnosis = DetailDiagnosis.find("diagnosis=? and gejala=?", diagnosis, gejala).first();
    			euclidian = Math.pow((bobotsom.bobot- detdiagnosis.derajat.nilai),2);
    			matriksmin.set(j, matriksmin.get(j)+euclidian);
    			System.out.println("id diagnosis  = " + id);
    		}
    	}
		
		int kelasterpilih = 0;
		for (int j=0;j<jumlahkelas.jumlahKelas; j++){
			if (nilaiminimum>matriksmin.get(j)){			
				nilaiminimum = matriksmin.get(j);
				kelasterpilih = j+1;
				System.out.println("kelas terpilih = " + kelasterpilih);
			}
		}
			
		//Retrieve
    	List listkasus = Kasus.find("kelas=?", kelasterpilih).fetch();    	
    	Iterator kas = listkasus.iterator();    	
    	while (kas.hasNext()){
    		Kasus k = (Kasus) kas.next();
    	
    		double m11 = 0;
    		double m00 = 0;
    		double jst = 1; //+1 usia 
    		double jt = 1; //+1 usia
    		
    		
    		if (k.usia>17 && diagnosis.usia>17){
    			m11 = m11+1;
    			m00 = 0;
    		}else
    			if(k.usia<17 && diagnosis.usia<17){
    				m11 = 0;
    				m00 = m00+1;
    			}
    		
    		List listgejala = Gejala.findAll();
    		Iterator gejala = listgejala.iterator();
    		while (gejala.hasNext()){
    			Gejala g = (Gejala) gejala.next();
    			DetailKasus detailkasus =DetailKasus.find("kasus=? and gejala=?" , k , g).first();
    			DetailDiagnosis detaildiagnosis =DetailDiagnosis.find("diagnosis=? and gejala=?" , diagnosis, g).first();
    			if (detaildiagnosis.derajat.nilai!=0){
	   				jt = jt +1 ;
    			}
	   			if (detaildiagnosis.derajat.nilai!=0 && detailkasus.derajat.nilai!=0){
	   				jst = jst +1;
	   				m11 = m11+1;
	   			}	   				
    			if(detaildiagnosis.derajat.nilai==0 && detailkasus.derajat.nilai==0){
	   					m00 = m00 + 1;
    			}
    		}    
    		System.out.println("Kode Kasus) = " + k.kode_kasus);
    		System.out.println("J(T) = " + jt);
        	System.out.println("J(S,T) = " + jst);
        	System.out.println("Match = " + (m11+m00));
        	System.out.println("Fitur = " + bobot);
        	
    		double hasilsmc = (double) (((m11+m00)/bobot)*(jst/jt))*100;
    		System.out.println("SMC = " + hasilsmc);
    		Resultindex rn = new Resultindex();
    		rn.kasus = k;
    		rn.diagnosis = diagnosis;
    		rn.hasilsmc = hasilsmc;
    		rn.save();
    		
    		jt = 1;
    		jst= 1;
    		m11 = 1;
    		m00 = 1;
    		
    	} 
    	//Reuse
    	//parameter maximum Similarity
       	double maxSimSMC = 0;
   
    	Resultindex  hasilSMC = null ;
    	List listresulinx = Resultindex.findAll();
    	Iterator ridx = listresulinx.iterator();
    	while (ridx.hasNext()){
    		Resultindex rI = (Resultindex) ridx.next();
    		
    		//Max Simple Matching Coefficient
    		if(maxSimSMC<rI.hasilsmc){
    			maxSimSMC = rI.hasilsmc;
    			hasilSMC = rI;
    		}
    	}
    	end = System.currentTimeMillis();
    	double waktu;
    	waktu =  (double) ((end - start) / 1000.0);
    	
        System.out.println("\nWaktu yang diperlukan selama proses adalah " + waktu + " detik");
        System.out.println("maksimum usia = " + maxusia);
        System.out.println("minimum usia = " + minusia);
        
        // Revisi
        if (maxSimSMC<80){
    	   Revisi revisi = new Revisi();
           revisi.nama_pasien = diagnosis.nama_pasien;
           revisi.usia = diagnosis.usia;
           revisi.smc = maxSimSMC;
           revisi.id_diagnosis = diagnosis.id;
           revisi.save();
           
        List listgejala = Gejala.findAll();
   		Iterator gejala = listgejala.iterator();
   		while (gejala.hasNext()){
   			Gejala g = (Gejala) gejala.next(); 
   			DetailDiagnosis detaildiagnosis =DetailDiagnosis.find("diagnosis=? and gejala=?" , diagnosis, g).first();
   			DetailRevisi detailRevisi = new DetailRevisi();
   			detailRevisi.gejala = detaildiagnosis.gejala;
   			detailRevisi.derajat = detaildiagnosis.derajat;
   			detailRevisi.revisi = Revisi.findById(revisi.id);
   			detailRevisi.save();
   		}
       }
        //retain
        if (maxSimSMC != 100){
        	Kasus k = new Kasus();
        	k.usia = diagnosis.usia;
        	k.penyakit = hasilSMC.kasus.penyakit;
        	k.save();
        	
        	 List listgejala = Gejala.findAll();
        		Iterator gejala = listgejala.iterator();
        		while (gejala.hasNext()){
        			Gejala g = (Gejala) gejala.next(); 
        			DetailDiagnosis detaildiagnosis =DetailDiagnosis.find("diagnosis=? and gejala=?" , diagnosis, g).first();
        			DetailKasus detailKasus = new DetailKasus();
        			detailKasus.gejala = detaildiagnosis.gejala;
        			detailKasus.derajat = detaildiagnosis.derajat;
        			detailKasus.kasus = Kasus.findById(k.id);
        			detailKasus.save();
        		} 
        }
		System.out.println("maxnya SMC:" + maxSimSMC);
		
		List listindex = Resultindex.findAll();  	   	  	
		render (listindex,userid, maxSimSMC, hasilSMC, waktu);  
    }
    public static void doindexmd(long id) {
    	Resultindex.deleteAll();
    	String userid = " ";
    	if(user!=null){
    		userid =user.level.id.toString();
    	}
    	
    	long start;
    	long end;
    	
    	start = System.currentTimeMillis();

    	
    	//parameter
    	int maxGejala = 3;
    	double bobot = 34;
    	int maxusia = 0;
    	int minusia = 100;
    	double nilaiminimum = 100;
    	int jumlahgejala = (int) Gejala.count();
   	
    	List listkasuss= Kasus.findAll();
    	Iterator kasus = listkasuss.iterator();
    	while (kasus.hasNext()){
    		Kasus k = (Kasus) kasus.next();
    		if(maxusia<k.usia){
    			maxusia = k.usia;
    		}
    		if(minusia>k.usia){
    			minusia = k.usia;    			
    		}
    	}
    	//indexing
    	Diagnosis diagnosis = Diagnosis.findById(id);
    	ArrayList<Double> matriksmin = new ArrayList<Double>();
    	for (int j=0;j<jumlahkelas.jumlahKelas; j++){
			matriksmin.add(0.0);
		}
    	
    	double euclidian = 0.0;		
		for (int i=0;i<jumlahgejala;i++){
    		for (int j=0;j<jumlahkelas.jumlahKelas; j++){
    			Gejala gejala = Gejala.findById((long)(i+1));
    			Som bobotsom = Som.find("gejala=? and kelas=?", gejala, (j+1)).first();
    			DetailDiagnosis detdiagnosis = DetailDiagnosis.find("diagnosis=? and gejala=?", diagnosis, gejala).first();
    			euclidian = Math.pow((bobotsom.bobot- detdiagnosis.derajat.nilai),2);
    			matriksmin.set(j, matriksmin.get(j)+euclidian);
    		}
    	}
		
		int kelasterpilih = 0;
		for (int j=0;j<jumlahkelas.jumlahKelas; j++){
			if (nilaiminimum>matriksmin.get(j)){			
				nilaiminimum = matriksmin.get(j);
				kelasterpilih = j+1;
				System.out.println("kelas terpilih = " + kelasterpilih);
			}
		}
		
		nilaiminimum = 100;
			
		//retrieve
    	List listkasus = Kasus.find("kelas=?", kelasterpilih).fetch();    	
    	Iterator kas = listkasus.iterator();    	
    	while (kas.hasNext()){
    		Kasus k = (Kasus) kas.next();    		
    		double simlokalusiaminkowski = simlokalusia(maxusia, minusia, k.usia, diagnosis.usia,2);
    		double simlokalgejalaminkowski = 0;
    		double jst = 1; //+1 usia 
    		double jt = 1;
    		
    		
    		List listgejala = Gejala.findAll();
    		Iterator gejala = listgejala.iterator();
    		while (gejala.hasNext()){
    			Gejala g = (Gejala) gejala.next();
    			DetailKasus detailkasus =DetailKasus.find("kasus=? and gejala=?" , k , g).first();
    			DetailDiagnosis detaildiagnosis =DetailDiagnosis.find("diagnosis=? and gejala=?" , diagnosis, g).first();
    			simlokalgejalaminkowski = simlokalgejalaminkowski + simlokalgejala(maxGejala,detailkasus.derajat.nilai , detaildiagnosis.derajat.nilai,2);
    			if (detaildiagnosis.derajat.nilai!=0){
	   				jt = jt +1 ;
    			}
	   			if (detaildiagnosis.derajat.nilai!=0 && detailkasus.derajat.nilai!=0){
	   				jst = jst +1;
	   			}	   				
    		}    		
    		System.out.println("Kode Kasus = " + k.kode_kasus);
    		System.out.println("JST = " + jst);
    		System.out.println("JT = " + jt);
    		System.out.println("Bobot = " + bobot);
    		System.out.println("SImilaritas Fitur Usia = " + simlokalusiaminkowski);
    		System.out.println("SImilaritas Fitur Gejala = " + simlokalgejalaminkowski);
    		
    		double hasilminkowski = (double) Math.cbrt((((simlokalgejalaminkowski + simlokalusiaminkowski))/bobot))*(jst/jt)*100;
    		System.out.println("Hasil MD = " + hasilminkowski);
    		Resultindex rn = new Resultindex();
    		rn.kasus = k;
    		rn.diagnosis = diagnosis;
    		rn.hasilminkowski = hasilminkowski;    		
    		rn.save();
    		
    		jt = 1;
    		jst= 1;
    		
    	} 
    	//reuse
    	double maxSimMD = 0;
    	Resultindex hasilMD = null;
    	List listresulinx = Resultindex.findAll();
    	Iterator ridx = listresulinx.iterator();
    	while (ridx.hasNext()){
    		Resultindex rI = (Resultindex) ridx.next();
    		//Max Minkowski
    		if(maxSimMD<rI.hasilminkowski){
    			maxSimMD = rI.hasilminkowski;
    			hasilMD = rI;
    		}
    	}
    	end = System.currentTimeMillis();
    	double waktu;
    	waktu =  (double) ((end - start) / 1000.0);
    	
        System.out.println("\nWaktu yang diperlukan selama proses adalah " + waktu + " detik");
        System.out.println("maksimum usia = " + maxusia);
        System.out.println("minimum usia = " + minusia);
        
        //Revisi
        if (maxSimMD<80){
    	   Revisi revisi = new Revisi();
           revisi.nama_pasien = diagnosis.nama_pasien;
           revisi.usia = diagnosis.usia;
           revisi.minkowskidistance = maxSimMD;
           revisi.id_diagnosis = diagnosis.id;
           revisi.save();
           
        List listgejala = Gejala.findAll();
   		Iterator gejala = listgejala.iterator();
   		while (gejala.hasNext()){
   			Gejala g = (Gejala) gejala.next(); 
   			DetailDiagnosis detaildiagnosis =DetailDiagnosis.find("diagnosis=? and gejala=?" , diagnosis, g).first();
   			DetailRevisi detailRevisi = new DetailRevisi();
   			detailRevisi.gejala = detaildiagnosis.gejala;
   			detailRevisi.derajat = detaildiagnosis.derajat;
   			detailRevisi.revisi = Revisi.findById(revisi.id);
   			detailRevisi.save();
   		}
       }
      //retain
        if (maxSimMD != 100 || maxSimMD >80){
        	Kasus k = new Kasus();
        	k.usia = diagnosis.usia;
        	k.penyakit = hasilMD.kasus.penyakit;
        	k.save();
        	
        	 List listgejala = Gejala.findAll();
        		Iterator gejala = listgejala.iterator();
        		while (gejala.hasNext()){
        			Gejala g = (Gejala) gejala.next(); 
        			DetailDiagnosis detaildiagnosis =DetailDiagnosis.find("diagnosis=? and gejala=?" , diagnosis, g).first();
        			DetailKasus detailKasus = new DetailKasus();
        			detailKasus.gejala = detaildiagnosis.gejala;
        			detailKasus.derajat = detaildiagnosis.derajat;
        			detailKasus.kasus = Kasus.findById(k.id);
        			detailKasus.save();
        		} 
        }
        
    	System.out.println("maxnya MD:" + maxSimMD);
		
    	List listindex = Resultindex.findAll();  	   	  	
       render (listindex,userid, maxSimMD, hasilMD, waktu);  
    }
    
    public static void noindexnn (long id){
    	Resultnearest.deleteAll();
    	String userid = " ";
    	if(user!=null){
    		userid =user.level.id.toString();
    	} 
    	long start;
    	long end;
    	start = System.currentTimeMillis();
    	
    	int maxGejala = 3;
    	double bobot = 34.0;
    	int maxusia = 0;   
    	int minusia = 100;
    	Diagnosis diagnosis = Diagnosis.findById(id);   	
    	List listkasus = Kasus.findAll();
    	Iterator kasus = listkasus.iterator();
    	while (kasus.hasNext()){
    		Kasus k = (Kasus) kasus.next();
    		if(maxusia<k.usia){
    			maxusia = k.usia;
    		}
    		if(minusia>k.usia){
    			minusia = k.usia;
    		}
    	}
    	//Retrieve
    	Iterator kas = listkasus.iterator();    	
    	while (kas.hasNext()){
    		Kasus k = (Kasus) kas.next();
    		double simlokalusia = simlokalusia(maxusia, minusia, k.usia, diagnosis.usia,1);
    		double simlokalgejala = 0.0;
    		double jst = 1.0; //+1 untuk usia
    		double jt = 1.0; // +1 untuk usia
    		
			
    		List listgejala = Gejala.findAll();
    		Iterator gejala = listgejala.iterator();
    		while (gejala.hasNext()){
    			Gejala g = (Gejala) gejala.next();
    			DetailKasus detailkasus =DetailKasus.find("kasus=? and gejala=?" , k , g).first();
    			DetailDiagnosis detaildiagnosis =DetailDiagnosis.find("diagnosis=? and gejala=?" , diagnosis, g).first();
    			simlokalgejala = simlokalgejala + simlokalgejala(maxGejala,detailkasus.derajat.nilai , detaildiagnosis.derajat.nilai,1);
    			if (detaildiagnosis.derajat.nilai!=0 && detailkasus.derajat.nilai!=0){
    				jst = jst+1;
    			}
    				if (detaildiagnosis.derajat.nilai!=0){
    					jt = jt+1;
    				} 
    		}
    		System.out.println("Kode Kasus = " + k.kode_kasus);
    		System.out.println("JST = " + jst);
    		System.out.println("JT = " + jt);
    		System.out.println("Bobot = " + bobot);
    		System.out.println("SImilaritas Fitur Usia = " + simlokalusia);
    		System.out.println("SImilaritas Fitur Gejala = " + simlokalgejala);
    		
    		double hasilnearest = (double) (((simlokalgejala + simlokalusia)/bobot)*(jst/jt))*100;
    		System.out.println("Hasil NN = " + hasilnearest);
    		
    		Resultnearest rn = new Resultnearest();
    		rn.kasus = k;
    		rn.diagnosis = diagnosis;
    		rn.hasilnearest = hasilnearest;
    		rn.save();
    		
    		jt = 1;
    		jst= 1;
    		
    	}
    	//Reuse
    	//parameter maximum Similarity
    	double maxSimNN = 0;
    	Resultnearest hasilNN = null;
    	List listRN = Resultnearest.findAll();
    	Iterator ridx = listRN.iterator();
    	while (ridx.hasNext()){
    		Resultnearest rI = (Resultnearest) ridx.next();
    		//Max Nearest Neighbor
    		if(maxSimNN<rI.hasilnearest){
    			maxSimNN = rI.hasilnearest;
    			hasilNN = rI;
    		}
    	}
    	end = System.currentTimeMillis();
    	System.out.println("\nWaktu yang diperlukan selama proses adalah " + ((end - start) / 1000.0) + " detik");
    	double waktu;
    	waktu = (double) ((end - start) / 1000.0);
    	System.out.println("NN :" + maxSimNN);
    	
    	//Revisi
        if (maxSimNN < 80){
    	   Revisi revisi = new Revisi();
           revisi.nama_pasien = diagnosis.nama_pasien;
           revisi.usia = diagnosis.usia;
           revisi.nearestneighbor = maxSimNN;
           revisi.id_diagnosis = diagnosis.id;
           revisi.save();
           
        List listgejala = Gejala.findAll();
   		Iterator gejala = listgejala.iterator();
   		while (gejala.hasNext()){
   			Gejala g = (Gejala) gejala.next(); 
   			DetailDiagnosis detaildiagnosis =DetailDiagnosis.find("diagnosis=? and gejala=?" , diagnosis, g).first();
   			DetailRevisi detailRevisi = new DetailRevisi();
   			detailRevisi.gejala = detaildiagnosis.gejala;
   			detailRevisi.derajat = detaildiagnosis.derajat;
   			detailRevisi.revisi = Revisi.findById(revisi.id);
   			detailRevisi.save();
   		}
       }
      //retain
        if (maxSimNN != 100){
        	Kasus k = new Kasus();
        	k.usia = diagnosis.usia;
        	k.penyakit = hasilNN.kasus.penyakit;
        	k.save();
        	
        	 List listgejala = Gejala.findAll();
        		Iterator gejala = listgejala.iterator();
        		while (gejala.hasNext()){
        			Gejala g = (Gejala) gejala.next(); 
        			DetailDiagnosis detaildiagnosis =DetailDiagnosis.find("diagnosis=? and gejala=?" , diagnosis, g).first();
        			DetailKasus detailKasus = new DetailKasus();
        			detailKasus.gejala = detaildiagnosis.gejala;
        			detailKasus.derajat = detaildiagnosis.derajat;
        			detailKasus.kasus = Kasus.findById(k.id);
        			detailKasus.save();
        		} 
        }
    	    	
    	List listrn = Resultnearest.findAll();  	   	  	
       render (listrn,userid, maxSimNN, hasilNN, waktu);
    	
    }
    
    public static void noindexmd (long id){
    	Resultnearest.deleteAll();
    	String userid = " ";
    	if(user!=null){
    		userid =user.level.id.toString();
    	} 
    	long start;
    	long end;
    	start = System.currentTimeMillis();
    	
    	int maxGejala = 3;
    	double bobot = 34.0;
    	int maxusia = 0;   
    	int minusia = 100;
    	
    	Diagnosis diagnosis = Diagnosis.findById(id);   	
    	List listkasus = Kasus.findAll();
    	Iterator kasus = listkasus.iterator();
    	while (kasus.hasNext()){
    		Kasus k = (Kasus) kasus.next();
    		if(maxusia<k.usia){
    			maxusia = k.usia;
    		}
    		if(minusia>k.usia){
    			minusia = k.usia;
    		}
    	}
    	
    	//Retrieve
    	Iterator kas = listkasus.iterator();    	
    	while (kas.hasNext()){
    		Kasus k = (Kasus) kas.next();
    		double simlokalusiaminkowski = simlokalusia(maxusia, minusia, k.usia, diagnosis.usia,2);
     		double simlokalgejalaminkowski = 0.0;
    		double jst = 1.0; //+1 untuk usia
    		double jt = 1.0; // +1 untuk usia
    					
    		List listgejala = Gejala.findAll();
    		Iterator gejala = listgejala.iterator();
    		while (gejala.hasNext()){
    			Gejala g = (Gejala) gejala.next();
    			DetailKasus detailkasus =DetailKasus.find("kasus=? and gejala=?" , k , g).first();
    			DetailDiagnosis detaildiagnosis =DetailDiagnosis.find("diagnosis=? and gejala=?" , diagnosis, g).first();
    			simlokalgejalaminkowski = simlokalgejalaminkowski + simlokalgejala(maxGejala,detailkasus.derajat.nilai , detaildiagnosis.derajat.nilai,2);
    			if (detaildiagnosis.derajat.nilai!=0 && detailkasus.derajat.nilai!=0){
    				jst = jst+1;
    			}
    				if (detaildiagnosis.derajat.nilai!=0){
    					jt = jt+1;
    				}
    		}
    		System.out.println("Kode Kasus = " + k.kode_kasus);
    		System.out.println("JST = " + jst);
    		System.out.println("JT = " + jt);
    		System.out.println("Bobot = " + bobot);
    		System.out.println("SImilaritas Fitur Usia = " + simlokalusiaminkowski);
    		System.out.println("SImilaritas Fitur Gejala = " + simlokalgejalaminkowski);
    		
    		double hasilminkowski = (double) Math.cbrt(((simlokalgejalaminkowski + simlokalusiaminkowski))/bobot)*(jst/jt)*100;
    		System.out.println("Hasil MD = " + hasilminkowski);
    		Resultnearest rn = new Resultnearest();
    		rn.kasus = k;
    		rn.diagnosis = diagnosis;
    		rn.hasilminkowski = hasilminkowski;
    		rn.save();
    		
    		jt = 1;
    		jst= 1;
    		
    	}
    	//Reuse
    	//parameter maximum Similarity
    	double maxSimMD = 0;
    	Resultnearest hasilMD = null;
    	List listRN = Resultnearest.findAll();
    	Iterator ridx = listRN.iterator();
    	while (ridx.hasNext()){
    		Resultnearest rI = (Resultnearest) ridx.next();
    		//Max Minkowski
    		if(maxSimMD<rI.hasilminkowski){
    			maxSimMD = rI.hasilminkowski;
    			hasilMD = rI;
    		}
    	}
    	end = System.currentTimeMillis();
    	System.out.println("\nWaktu yang diperlukan selama proses adalah " + ((end - start) / 1000.0) + " detik");
    	double waktu;
    	waktu = (double) ((end - start) / 1000.0);
    	System.out.println("MD :" + maxSimMD);
    	
    	//Revisi
        if (maxSimMD<80){
    	   Revisi revisi = new Revisi();
           revisi.nama_pasien = diagnosis.nama_pasien;
           revisi.usia = diagnosis.usia;
           revisi.minkowskidistance = maxSimMD;
           revisi.id_diagnosis = diagnosis.id;
           revisi.save();
           
        List listgejala = Gejala.findAll();
   		Iterator gejala = listgejala.iterator();
   		while (gejala.hasNext()){
   			Gejala g = (Gejala) gejala.next(); 
   			DetailDiagnosis detaildiagnosis =DetailDiagnosis.find("diagnosis=? and gejala=?" , diagnosis, g).first();
   			DetailRevisi detailRevisi = new DetailRevisi();
   			detailRevisi.gejala = detaildiagnosis.gejala;
   			detailRevisi.derajat = detaildiagnosis.derajat;
   			detailRevisi.revisi = Revisi.findById(revisi.id);
   			detailRevisi.save();
   		}
       }
      //retain
        if (maxSimMD != 100){
        	Kasus k = new Kasus();
        	k.usia = diagnosis.usia;
        	k.penyakit = hasilMD.kasus.penyakit;
        	k.save();
        	
        	 List listgejala = Gejala.findAll();
        		Iterator gejala = listgejala.iterator();
        		while (gejala.hasNext()){
        			Gejala g = (Gejala) gejala.next(); 
        			DetailDiagnosis detaildiagnosis =DetailDiagnosis.find("diagnosis=? and gejala=?" , diagnosis, g).first();
        			DetailKasus detailKasus = new DetailKasus();
        			detailKasus.gejala = detaildiagnosis.gejala;
        			detailKasus.derajat = detaildiagnosis.derajat;
        			detailKasus.kasus = Kasus.findById(k.id);
        			detailKasus.save();
        		} 
        }  	
    	
    	List listrn = Resultnearest.findAll();  	   	  	
       render (listrn,userid, maxSimMD, hasilMD, waktu);
    }
    
    public static void noindexsmc(long id) {
    	Resultnearest.deleteAll();
    	String userid = " ";
    	if(user!=null){
    		userid =user.level.id.toString();
    	} 
    	long start;
    	long end;
    	start = System.currentTimeMillis();
    	
    	double bobot = 34.0;
    	double m11 = 0.0;
		double m00 = 0.0;
    	Diagnosis diagnosis = Diagnosis.findById(id);   	
    	List listkasus = Kasus.findAll();
    	
    	Iterator kas = listkasus.iterator();    	
    	while (kas.hasNext()){
    		Kasus k = (Kasus) kas.next();
    		double jst = 1.0; //+1 untuk usia
    		double jt = 1.0; // +1 untuk usia
    		
    		if (k.usia>17 && diagnosis.usia>17){
    			m11 = m11+1;
    			m00 = 0;
    		}else
    			if(k.usia<17 && diagnosis.usia<17){
    				m11 = 0;
    				m00 = m00+1;
    			}
			
    		List listgejala = Gejala.findAll();
    		Iterator gejala = listgejala.iterator();
    		while (gejala.hasNext()){
    			Gejala g = (Gejala) gejala.next();
    			DetailKasus detailkasus =DetailKasus.find("kasus=? and gejala=?" , k , g).first();
    			DetailDiagnosis detaildiagnosis =DetailDiagnosis.find("diagnosis=? and gejala=?" , diagnosis, g).first();
    			if (detaildiagnosis.derajat.nilai!=0 && detailkasus.derajat.nilai!=0){
    				jst = jst+1;
    				m11 = m11+1;
    			}
    				if (detaildiagnosis.derajat.nilai!=0){
    					jt = jt+1;
    				}
    			else {
    				if(detaildiagnosis.derajat.nilai==0 && detailkasus.derajat.nilai==0){
    					m00 = m00 + 1;
    				}
    			}
    		}
    		
    		System.out.println("Kode Kasus) = " + k.kode_kasus);
    		System.out.println("J(T) = " + jt);
        	System.out.println("J(S,T) = " + jst);
        	System.out.println("Match = " + (m11+m00));
        	System.out.println("Fitur = " + bobot);
    		
    		double hasilsmc = (double) (((m11+m00)/bobot)*(jst/jt)*100);
    		System.out.println("Hasil SMC = " + hasilsmc);
    		
    		Resultnearest rn = new Resultnearest();
    		rn.kasus = k;
    		rn.diagnosis = diagnosis;
    		rn.hasilsmc = hasilsmc;
    		rn.save();
    		
    		jt = 0;
    		jst= 0;
    		m11 = 0;
    		m00 = 0;
    		
    	}
    	//parameter maximum Similarity
    	double maxSimSMC = 0;
    	Resultnearest  hasilSMC = null ;
    	List listRN = Resultnearest.findAll();
    	Iterator ridx = listRN.iterator();
    	while (ridx.hasNext()){
    		Resultnearest rI = (Resultnearest) ridx.next();
    		//Max Simple Matching Coefficient
    		if(maxSimSMC<rI.hasilsmc){
    			maxSimSMC = rI.hasilsmc; 
    			hasilSMC = rI;
    		}
    	}
    	end = System.currentTimeMillis();
    	System.out.println("\nWaktu yang diperlukan selama proses adalah " + ((end - start) / 1000.0) + " detik");
    	double waktu;
    	waktu = (double) ((end - start) / 1000.0);
    	
    	//Revisi
        if (maxSimSMC<80){
    	   Revisi revisi = new Revisi();
           revisi.nama_pasien = diagnosis.nama_pasien;
           revisi.usia = diagnosis.usia;
           revisi.smc = maxSimSMC;
           revisi.id_diagnosis = diagnosis.id;
           revisi.save();
           
        List listgejala = Gejala.findAll();
   		Iterator gejala = listgejala.iterator();
   		while (gejala.hasNext()){
   			Gejala g = (Gejala) gejala.next(); 
   			DetailDiagnosis detaildiagnosis =DetailDiagnosis.find("diagnosis=? and gejala=?" , diagnosis, g).first();
   			DetailRevisi detailRevisi = new DetailRevisi();
   			detailRevisi.gejala = detaildiagnosis.gejala;
   			detailRevisi.derajat = detaildiagnosis.derajat;
   			detailRevisi.revisi = Revisi.findById(revisi.id);
   			detailRevisi.save();
   		}
       }
      //retain
        if (maxSimSMC != 100){
        	Kasus k = new Kasus();
        	k.usia = diagnosis.usia;
        	k.penyakit = hasilSMC.kasus.penyakit;
        	k.save();
        	
        	 List listgejala = Gejala.findAll();
        		Iterator gejala = listgejala.iterator();
        		while (gejala.hasNext()){
        			Gejala g = (Gejala) gejala.next(); 
        			DetailDiagnosis detaildiagnosis =DetailDiagnosis.find("diagnosis=? and gejala=?" , diagnosis, g).first();
        			DetailKasus detailKasus = new DetailKasus();
        			detailKasus.gejala = detaildiagnosis.gejala;
        			detailKasus.derajat = detaildiagnosis.derajat;
        			detailKasus.kasus = Kasus.findById(k.id);
        			detailKasus.save();
        		} 
        }
    	
    	System.out.println("SMC :" + maxSimSMC);
    	List listrn = Resultnearest.findAll();  	   	  	
       render (listrn,userid, maxSimSMC,hasilSMC, waktu);
    	
    }
      
    public static double simlokalusia(double maxusia,double minusia, double usiakasus, double usiadiagnosis,int kode){
    	double simlokal = 0.0;
    	if (kode==1){ //nearest
    		simlokal = (double) (((double) 1- (Math.abs((double) ((usiakasus-usiadiagnosis)/(maxusia-minusia))))));
    	}else if (kode == 2) { //minkowski
    		simlokal = (double) Math.pow((( (double) 1- (Math.abs((double) ((usiakasus-usiadiagnosis)/(maxusia-minusia))) ))) ,3);
    	}
    	return simlokal;
    }
    
    public static double simlokalgejala(double maxgejala, double gejalakasus, double gejaladiagnosis,int kode){
    	double simlokal = 0.0;
    	if(kode==1){
    		simlokal = (double) ((double) 1- (Math.abs((double) ((gejalakasus-gejaladiagnosis)/maxgejala))));
    	}else if (kode ==2){
    		simlokal = (double) Math.pow ((((double) 1- (Math.abs((double) ((gejalakasus-gejaladiagnosis)/maxgejala)) ))),3);
    	}
    	return simlokal;
    }   
    
    public static void penyakit(){
    	String userid = " ";
    	if(user!=null){
    		userid =user.level.id.toString();
    	}
    	List listpenyakit= Penyakit.findAll();
    	render (listpenyakit, userid);
    }
    
    public static void revisi(){
    	String userid = " ";
    	if(user!=null){
    		userid =user.level.id.toString();
    	}
    	List listrevisi= Revisi.findAll();
    	render (listrevisi, userid);
    }
    
    public static void retain(long id){
    	String userid = " ";
    	if(user!=null){
    		userid =user.level.id.toString();
    	}
    	Revisi revisi = Revisi.findById(id);
    	Kasus kasus = new Kasus();
    	kasus.penyakit = revisi.penyakit;
    	kasus.usia = revisi.usia;
    	kasus.save();
         
    	List listgejala = Gejala.findAll();
 		Iterator gejala = listgejala.iterator();
 		while (gejala.hasNext()){
 			Gejala g = (Gejala) gejala.next(); 
 			DetailRevisi detailrevisi = DetailRevisi.find("revisi=? and gejala=?" , revisi, g).first();
 			DetailKasus detailkasus = new DetailKasus();
 			detailkasus.gejala = detailrevisi.gejala;
 			detailkasus.derajat = detailrevisi.derajat;
 			detailkasus.kasus = Kasus.findById(kasus.id);
 			detailkasus.save();
 		}
 		revisi();
 	 	render (userid);
    }
    
    public static void editpenyakit(long id){
    	String userid = " ";
    	if(user!=null){
    		userid =user.level.id.toString();
    	}
    	Penyakit objek;
		objek = Penyakit.find("id=?", id).first();
    	if
    	(objek == null){
    		objek = new Penyakit();
    	}
    	render (objek, userid);
    }
    public static void simpanpenyakit(@Valid Penyakit objek){
    	Penyakit g;
    	g = objek;
    	if(validation.hasErrors()){
    		params.flash();
    		validation.keep();
    		editpenyakit(g.id);
    	}
    	g.save();
    	penyakit();
    }
    public static void simpanrevisi (@Valid Revisi objek){
    	Revisi r;
    	r = objek;
    	if (validation.hasErrors()){
    		params.flash();
    		validation.keep();
    		editrevisi(r.id);
    	}
    	r.save();
    	revisi();
    }
    public static void hapuspenyakit(long id){
    	Penyakit.delete("id=?", id);
    	penyakit();
    }
        
    
    public static void gejala(){
    	String userid = " ";
    	if(user!=null){
    		userid =user.level.id.toString();
    	}
    	List listgejala = Gejala.findAll();
    	render (listgejala, userid);
    }
    public static void editgejala(long id){
    	String userid = " ";
    	if(user!=null){
    		userid =user.level.id.toString();
    	}
    	Gejala objekG;
    	objekG = Gejala.find("id=?", id).first();
    	if
    	(objekG == null){
    		objekG = new Gejala();
    	}
    	
    	render (objekG, userid);
    }
 
    public static void simpanG(@Valid Gejala objekG){
    	Gejala g;
    	g = objekG;
    	if(validation.hasErrors()){
    		params.flash();
    		validation.keep();
    		editgejala(g.id);
    	}
    	g.save();
    	gejala();
    }
    public static void hapusG(long id){
    	Gejala.delete("id=?", id);
    	gejala();
    }
   public static void kasus(){
	   String userid = " ";
   	if(user!=null){
   		userid =user.level.id.toString();
   	}
	   List listkasus= Kasus.findAll();
	   render(listkasus, userid);
   }
   
   public static void pengujian(){
	   String userid = " ";
   	if(user!=null){
   		userid =user.level.id.toString();
   	}
   	List listkasusuji = KasusUji.findAll();
    List listdetailkasusuji = DetailKasusUji.findAll();
	   render(userid, listdetailkasusuji, listkasusuji);
   }
   
   public static void pilihanpengujian(long id) {
   	String userid = " ";
   	if(user!=null){
   		userid =user.level.id.toString();
   	}
   	render (userid, id);
   }
   public static void detailpengujiannoindex (){
	 String userid = " ";
	   	if(user!=null){
	   		userid =user.level.id.toString();
	   	}
		List listhasilujinoindex = HasilUjiNoIndex.findAll();
	   render (userid, listhasilujinoindex);
	   
	   }
   public static void pengujiannoindexnn(){
		String userid = " ";
	   	if(user!=null){
	   		userid =user.level.id.toString();
	   	}
	   	HasilUjiNearest.deleteAll();
	    List<HasilUjiNearest> listnearestneighbor = new ArrayList<HasilUjiNearest>();
	    
	   	double waktu = 0;
	    long start;
	    long end;
	    int jumlahkasusuji = 0;
		int kasusbenar = 0;
	    
	   	//parameter
	   	int maxGejala = 3;
	   	double bobot = 34;
	   	int maxusia = 0;
	   	int minusia=100;
	   	
		List listkasuss = Kasus.findAll();
		Iterator kasus = listkasuss.iterator();
	   	while (kasus.hasNext()){
	   		Kasus k = (Kasus) kasus.next();
	   		if(maxusia<k.usia){
	   			maxusia = k.usia;
	   		}
	   		if(minusia>k.usia){
	   			minusia = k.usia;
	   		}		   	
	   	}
	   	List listkasusuji = KasusUji.findAll();
	   	Iterator kasusujinya = listkasusuji.iterator();
	   	jumlahkasusuji = listkasusuji.size();
	   	
	   	while (kasusujinya.hasNext()){
	   		waktu = 0;
	   		start = System.currentTimeMillis();
		   		KasusUji kasusuji = (KasusUji) kasusujinya.next(); 	
		   		System.out.println("kasus uji ke = " + kasusuji.kode_kasus);
		   		
			   	List listkasus = Kasus.findAll();		   	
			   	Iterator kas = listkasus.iterator();    	
			   	while (kas.hasNext()){
			   		Kasus k = (Kasus) kas.next();
			   		
			   		double simlokalusia = simlokalusia(maxusia, minusia, k.usia, kasusuji.usia,1);
			   		double simlokalgejala = 0;
			   		double jst = 1;
			   		double jt = 1;
			   		
		
			   		List listgejala = Gejala.findAll();
			   		Iterator gejala = listgejala.iterator();
			   		while (gejala.hasNext()){
			   			Gejala g = (Gejala) gejala.next();
			   			DetailKasus detailkasus =DetailKasus.find("kasus=? and gejala=?" , k , g).first();
			   			DetailKasusUji detailkasusuji =DetailKasusUji.find("kasusuji=? and gejala=?" , kasusuji, g).first();
			   			simlokalgejala = simlokalgejala + simlokalgejala(maxGejala,detailkasus.derajat.nilai , detailkasusuji.derajat.nilai,1);
			   			if (detailkasusuji.derajat.nilai!=0){
			   				jt = jt +1 ;
			   			}
			   				if (detailkasusuji.derajat.nilai!=0 && detailkasus.derajat.nilai!=0){
			   					jst = jst +1;
			   				}       		
			   		}
			   		
			   		System.out.println("Kode Kasus = " + k.kode_kasus);
		    		System.out.println("JST = " + jst);
		    		System.out.println("JT = " + jt);
		    		System.out.println("Bobot = " + bobot);
		    		System.out.println("SImilaritas Fitur Usia = " + simlokalusia);
		    		System.out.println("SImilaritas Fitur Gejala = " + simlokalgejala);
		    		
			   		double hasilnearest = (double) (((simlokalgejala + simlokalusia)/bobot)*(jst/jt))*100;	   		
			   		System.out.println("Nearest Neighbor = " + hasilnearest);
			   		
			   		HasilUjiNearest rn = new HasilUjiNearest();
			   		rn.kasus = k;
			   		rn.kasusuji = kasusuji;
			   		rn.hasilnearest = hasilnearest;
			   		rn.save();
			   		
			   		jt = 1;
			   		jst= 1;
			   	}
			   	//parameter maximum Similarity
			   	double maxSimNN = 0;
			   	
			   	//Hasil Uji Nearest
			   	HasilUjiNearest resultNN = null;
			   	List listnearest = HasilUjiNearest.find("kasusuji=?", kasusuji).fetch();
			   	Iterator rN = listnearest.iterator();
			   	while (rN.hasNext()){
			   		HasilUjiNearest rNearest = (HasilUjiNearest) rN.next();
			   		if(maxSimNN<rNearest.hasilnearest){
			   			maxSimNN = rNearest.hasilnearest;
			   			resultNN= rNearest;
			   		}
			   	}			  
				listnearestneighbor.add(resultNN);
				end = System.currentTimeMillis();
				waktu = (double) (((end-start)/1000.0)/jumlahkasusuji);
				
				if(resultNN.kasusuji.penyakit == resultNN.kasus.penyakit){
					kasusbenar = kasusbenar + 1;
				}
			}   
			render (listnearestneighbor, userid, waktu, kasusbenar);  	
   }
   
   public static void pengujiannoindexmd(){
		HasilUjiMinkowski.deleteAll();
	    List<HasilUjiMinkowski> listminkowskidistance = new ArrayList<HasilUjiMinkowski>();

	   	String userid = " ";
	   	if(user!=null){
	   		userid =user.level.id.toString();
	   	}
	   	int jumlahkasusuji = 0;
	   
	   	int kasusbenar = 0;
	   	double akurasi = 0;
	   	double waktu = 0;
	    long start;
	    long end;
	    
	   	//parameter
	   	int maxGejala = 3;
	   	double bobot = 34;
	   	int maxusia = 0;
	   	int minusia=100;
	   	List listkasuss = Kasus.findAll();
		Iterator kasus = listkasuss.iterator();
	   	while (kasus.hasNext()){
	   		Kasus k = (Kasus) kasus.next();
	   		if(maxusia<k.usia){
	   			maxusia = k.usia;
	   		}
	   		if(minusia>k.usia){
	   			minusia = k.usia;
	   		}		   	
	   	}
	   	
	   	List listkasusuji = KasusUji.findAll();
		jumlahkasusuji = listkasusuji.size();
	   	Iterator kasusujinya = listkasusuji.iterator();
	   	while (kasusujinya.hasNext()){
	   		waktu = 0;
	   		start = System.currentTimeMillis();
		   		KasusUji kasusuji = (KasusUji) kasusujinya.next(); 	
		   		System.out.println("kasus uji ke = " + kasusuji.kode_kasus);
		   		
			   	List listkasus = Kasus.findAll();	   	
			   	Iterator kas = listkasus.iterator();    	
			   	while (kas.hasNext()){
			   		Kasus k = (Kasus) kas.next();
			   		
			   		double simlokalusiaminkowski = simlokalusia(maxusia, minusia, k.usia, kasusuji.usia,2);
			   		double simlokalgejalaminkowski = 0;
			   		double jst = 1;
			   		double jt = 1;
			   		
		
			   		List listgejala = Gejala.findAll();
			   		Iterator gejala = listgejala.iterator();
			   		while (gejala.hasNext()){
			   			Gejala g = (Gejala) gejala.next();
			   			DetailKasus detailkasus =DetailKasus.find("kasus=? and gejala=?" , k , g).first();
			   			DetailKasusUji detailkasusuji =DetailKasusUji.find("kasusuji=? and gejala=?" , kasusuji, g).first();
			   			simlokalgejalaminkowski = simlokalgejalaminkowski + simlokalgejala(maxGejala,detailkasus.derajat.nilai , detailkasusuji.derajat.nilai,2);
			   			if (detailkasusuji.derajat.nilai!=0){
			   				jt = jt +1 ;
			   			}
			   				if (detailkasusuji.derajat.nilai!=0 && detailkasus.derajat.nilai!=0){
			   					jst = jst +1;
			   				}      		
			   		}			   		
			   		double hasilminkowski = (double) Math.cbrt(((simlokalgejalaminkowski + simlokalusiaminkowski))/bobot)*(jst/jt)*100;
			   		
			   		HasilUjiMinkowski rm = new HasilUjiMinkowski();
			   		rm.kasus = k;
			   		rm.kasusuji = kasusuji;
			   		rm.hasilminkowski = hasilminkowski;
			   		rm.save();
			   					   		
			   		jt = 1;
			   		jst= 1;

			   	}
			   	//parameter maximum Similarity
			   	double maxSimMD = 0;
			   		
			   	//Hasil Uji Minkowski
			   	HasilUjiMinkowski resultMD = null;
			 	List listminkowski = HasilUjiMinkowski.find("kasusuji=?", kasusuji).fetch();
			   	Iterator rM = listminkowski.iterator();
			   	while (rM.hasNext()){
			   		HasilUjiMinkowski rMinkowski = (HasilUjiMinkowski) rM.next();
			   		if(maxSimMD<rMinkowski.hasilminkowski){
			   			maxSimMD = rMinkowski.hasilminkowski;
			   			resultMD= rMinkowski;
			   		}
			   	}
				listminkowskidistance.add(resultMD);
				end = System.currentTimeMillis();
				waktu = (double) ((end-start)/1000.0)/jumlahkasusuji;	
				
				if(resultMD.kasusuji.penyakit == resultMD.kasus.penyakit){
					kasusbenar = kasusbenar + 1;
		   		}
		   			akurasi = (double)(kasusbenar/jumlahkasusuji)*100;
			}
		render (listminkowskidistance, userid, waktu, kasusbenar, akurasi);  
   }
   public static void pengujiannoindexsmc(){
		HasilUjiSMC.deleteAll();
	    List<HasilUjiSMC> listsimplematching = new ArrayList<HasilUjiSMC>();
	   	String userid = " ";
	   	if(user!=null){
	   		userid =user.level.id.toString();
	   	}
	   	double waktu = 0;
	    long start;
	    long end;
	    int jumlahkasusuji = 0;
		int kasusbenar = 0;
	    
	   	//parameter
	   	double bobot = 34;
	   	
	   	List listkasusuji = KasusUji.findAll();
	   	Iterator kasusujinya = listkasusuji.iterator();
	   	jumlahkasusuji = listkasusuji.size();
	   	
	   	while (kasusujinya.hasNext()){
	   		waktu = 0;
	   		start = System.currentTimeMillis();
		   		KasusUji kasusuji = (KasusUji) kasusujinya.next(); 	
		   		System.out.println("kasus uji ke = " + kasusuji.kode_kasus);
		   		
			   	List listkasus = Kasus.findAll();			   	
			   	Iterator kas = listkasus.iterator();    	
			   	while (kas.hasNext()){
			   		Kasus k = (Kasus) kas.next();
			  
			   		double m11 = 0;
			   		double m00 = 0;
			   		double jst = 1;
			   		double jt = 1;
			   		
			   		if (k.usia>17 && kasusuji.usia>17){
		    			m11 = m11+1;
		    			m00 = 0;
		    		}else
		    			if(k.usia<17 && kasusuji.usia<17){
		    				m11 = 0;
		    				m00 = m00+1;
		    			}
		
			   		List listgejala = Gejala.findAll();
			   		Iterator gejala = listgejala.iterator();
			   		while (gejala.hasNext()){
			   			Gejala g = (Gejala) gejala.next();
			   			DetailKasus detailkasus =DetailKasus.find("kasus=? and gejala=?" , k , g).first();
			   			DetailKasusUji detailkasusuji =DetailKasusUji.find("kasusuji=? and gejala=?" , kasusuji, g).first();
			   			if (detailkasusuji.derajat.nilai!=0){
			   				jt = jt +1 ;
			   				if (detailkasusuji.derajat.nilai!=0 && detailkasus.derajat.nilai!=0){
			   					jst = jst +1;
			   					m11 = m11+1;
			   				}
			   			}else {
			   				if(detailkasusuji.derajat.nilai==0 && detailkasus.derajat.nilai==0){
			   					m00 = m00 + 1;
			   				}
			   			}		       		
			   		}
			   		double hasilsmc = (double) (((m11+m00)/bobot)*(jst/jt)*100);		   		
			   		HasilUjiSMC rsmc = new HasilUjiSMC();
			   		rsmc.kasus = k;
			   		rsmc.kasusuji = kasusuji;
			   		rsmc.hasilsmc = hasilsmc;
			   		rsmc.save();
			   		
			   		jt = 1;
			   		jst= 1;
			   		m11 = 0;
			   		m00 = 0;
			   	}
			   	//parameter maximum Similarity
			   	double maxSimSMC = 0;
			   	
			   	//Hasil Uji SMC
			   	HasilUjiSMC resultSMC = null;
			 	List listsmc = HasilUjiSMC.find("kasusuji=?", kasusuji).fetch();
			   	Iterator rSMC = listsmc.iterator();
			   	while (rSMC.hasNext()){
			   		HasilUjiSMC rSimpleMatch = (HasilUjiSMC) rSMC.next();
			   		if(maxSimSMC<rSimpleMatch.hasilsmc){
			   			maxSimSMC = rSimpleMatch.hasilsmc;
			   			resultSMC= rSimpleMatch;
			   		} 	
			   	}
				listsimplematching.add(resultSMC);
				end = System.currentTimeMillis();
				waktu = (double) (((end-start)/1000.0)/jumlahkasusuji);
				if(resultSMC.kasusuji.penyakit == resultSMC.kasus.penyakit){
					kasusbenar = kasusbenar + 1;
				}
			}    	
			render (listsimplematching, userid, waktu, kasusbenar);  
   }
   public static void pengujiannoindex(){
	   String userid = " ";
	   	if(user!=null){
	   		userid =user.level.id.toString();
	   	}
	   	double waktu = 0;
	   	long start;
	   	long end;
	   
		HasilUjiNoIndex.deleteAll();
//	    List<HasilUjiNoIndex> listhasilujinoindex = new ArrayList<HasilUjiNoIndex>();
	    List<HasilUjiNearest> listnearestneighbor = new ArrayList<HasilUjiNearest>();
	    List<HasilUjiMinkowski> listminkowskidistance = new ArrayList<HasilUjiMinkowski>();
	    List<HasilUjiSMC> listsimplematching = new ArrayList<HasilUjiSMC>();
		int maxGejala = 3;
	   	double bobot = 34;
	   	int maxusia = 0;
	   	int minusia = 100;
	   	
	   	List listkasusuji = KasusUji.findAll();
	   	Iterator kasusujinya = listkasusuji.iterator();
	   	
	   	while (kasusujinya.hasNext()){
	   		start = System.currentTimeMillis();
		   		KasusUji kasusuji = (KasusUji) kasusujinya.next(); 	
			   	ArrayList<Double> matriksmin = new ArrayList<Double>();
			   	for (int j=0;j<jumlahkelas.jumlahKelas; j++){
						matriksmin.add(0.0);
				}
			   	List listkasus = Kasus.findAll();
			   	Iterator kasus = listkasus.iterator();
			   	while (kasus.hasNext()){
			   		Kasus k = (Kasus) kasus.next();
			   		if(maxusia<k.usia){
			   			maxusia = k.usia;
			   		}
			   		if (minusia>k.usia){
			   			minusia = k.usia;
			   		}			   		
			   		
			   	}
			   
			   	Iterator kas = listkasus.iterator();    	
			   	while (kas.hasNext()){
			   		Kasus k = (Kasus) kas.next();
			   		
			   		double simlokalusia = simlokalusia(maxusia, minusia, k.usia, kasusuji.usia,1);
			   		double simlokalusiaminkowski = simlokalusia(maxusia, minusia, k.usia, kasusuji.usia,2);
			   		double simlokalgejala = 0;
			   		double simlokalgejalaminkowski = 0;
			   		double m11 = 0;
			   		double m00 = 0;
			   		double jst = 1; //untuk usia
			   		double jt = 1; //untuk usia
			   		
			   		//kesamaan fitur usia metode SMC
			   		if (k.usia>17 && kasusuji.usia>17){
		    			m11 = m11+1;
		    			m00 = 0;
		    		}else
		    			if(k.usia<17 && kasusuji.usia<17){
		    				m11 = 0;
		    				m00 = m00+1;
		    			}
		
			   		List listgejala = Gejala.findAll();
			   		Iterator gejala = listgejala.iterator();
			   		while (gejala.hasNext()){
			   			Gejala g = (Gejala) gejala.next();
			   			DetailKasus detailkasus =DetailKasus.find("kasus=? and gejala=?" , k , g).first();
			   			DetailKasusUji detailkasusuji =DetailKasusUji.find("kasusuji=? and gejala=?" , kasusuji, g).first();
			   			simlokalgejala = simlokalgejala + simlokalgejala(maxGejala,detailkasus.derajat.nilai , detailkasusuji.derajat.nilai,1);
			   			simlokalgejalaminkowski = simlokalgejalaminkowski + simlokalgejala(maxGejala,detailkasus.derajat.nilai , detailkasusuji.derajat.nilai,2);
			   			if (detailkasusuji.derajat.nilai!=0 && detailkasus.derajat.nilai!=0){
			   				jst = jst +1 ;
			   				m11 = m11+1; //fitur match metode SMC
			   				if (detailkasus.derajat.nilai!=0){
			   					jt = jt +1;
			   				}
			   			}else {
			   				if(detailkasusuji.derajat.nilai==0 && detailkasus.derajat.nilai==0){
			   					m00 = m00 + 1; //fitur match metode SMC
			   				}
			   			}
			   						       		
			   		}
			   		double hasilnearest = (double) (((simlokalgejala + simlokalusia)/bobot)*(jst/jt))*100;
			   		double hasilminkowski = (double) Math.cbrt(((simlokalgejalaminkowski + simlokalusiaminkowski))/bobot)*(jst/jt)*100;
			   		double hasilsmc = (double) (((m11+m00)/bobot)*(jst/jt)*100);
			   		
			   		HasilUjiNearest rn = new HasilUjiNearest();
			   		rn.kasus = k;
			   		rn.kasusuji = kasusuji;
			   		rn.hasilnearest = hasilnearest;
			   		rn.save();
			   		
			   		HasilUjiMinkowski rm = new HasilUjiMinkowski();
			   		rm.kasus = k;
			   		rm.kasusuji = kasusuji;
			   		rm.hasilminkowski = hasilminkowski;
			   		rm.save();
			   		
			   		HasilUjiSMC rsmc = new HasilUjiSMC();
			   		rsmc.kasus = k;
			   		rsmc.kasusuji = kasusuji;
			   		rsmc.hasilsmc = hasilsmc;
			   		rsmc.save();
			   		
			   		jt = 0;
			   		jst= 0;
			   		m11 = 0;
			   		m00 = 0;
			   	}
				System.out.println("id diagnosis = " + kasusuji.kode_kasus);
				
			   	//parameter maximum Similarity
			   	double maxSimMD = 0;
			   	double maxSimNN = 0;
			   	double maxSimSMC = 0;
			   	
			   	//Hasil Uji Nearest
			   	HasilUjiNearest resultNN = null;
			   	List listnearest = HasilUjiNearest.find("kasusuji=?", kasusuji).fetch();
			   	Iterator rN = listnearest.iterator();
			   	while (rN.hasNext()){
			   		HasilUjiNearest rNearest = (HasilUjiNearest) rN.next();
			   		if(maxSimNN<rNearest.hasilnearest){
			   			maxSimNN = rNearest.hasilnearest;
			   			resultNN= rNearest;
			   		}
			   	}
			   		
			   	//Hasil Uji Minkowski
			   	HasilUjiMinkowski resultMD = null;
			 	List listminkowski = HasilUjiMinkowski.find("kasusuji=?", kasusuji).fetch();
			   	Iterator rM = listminkowski.iterator();
			   	while (rM.hasNext()){
			   		HasilUjiMinkowski rMinkowski = (HasilUjiMinkowski) rM.next();
			   		if(maxSimMD<rMinkowski.hasilminkowski){
			   			maxSimMD = rMinkowski.hasilminkowski;
			   			resultMD= rMinkowski;
			   		}	
			   	}
			   	//Hasil Uji SMC
			   	HasilUjiSMC resultSMC = null;
			 	List listsmc = HasilUjiSMC.find("kasusuji=?", kasusuji).fetch();
			   	Iterator rSMC = listsmc.iterator();
			   	while (rSMC.hasNext()){
			   		HasilUjiSMC rSimpleMatch = (HasilUjiSMC) rSMC.next();
			   		if(maxSimSMC<rSimpleMatch.hasilsmc){
			   			maxSimSMC = rSimpleMatch.hasilsmc;
			   			resultSMC= rSimpleMatch;
			   		} 	
			   	}
			   	listnearestneighbor.add(resultNN);
				listminkowskidistance.add(resultMD);
				listsimplematching.add(resultSMC);
				end = System.currentTimeMillis();
			   	waktu = (double)((end-start)/1000.0);
			}					   	  	
	   render (listnearestneighbor, listminkowskidistance, listsimplematching,userid, waktu); 	   
   }
   
   //pengujian dengan indexing SOM
  public static void pengujiandoindex() {
   	HasilUjiIndex.deleteAll();
    List<HasilUjiIndexNearest> listnearestneighbor = new ArrayList<HasilUjiIndexNearest>();
    List<HasilUjiIndexMinkowski> listminkowskidistance = new ArrayList<HasilUjiIndexMinkowski>();
    List<HasilUjiIndexSMC> listsimplematching = new ArrayList<HasilUjiIndexSMC>();
   	String userid = " ";
   	if(user!=null){
   		userid =user.level.id.toString();
   	}
   	double waktu = 0;
    long start;
    long end;
    
   	//parameter
   	int maxGejala = 3;
   	double bobot = 34;
   	int maxusia = 0;
   	int minusia=100;
   	double nilaiminimum = 100;
   	int jumlahgejala = (int) Gejala.count();
   	
   	List listkasusuji = KasusUji.findAll();
   	Iterator kasusujinya = listkasusuji.iterator();
   	while (kasusujinya.hasNext()){
   		waktu = 0;
   		start = System.currentTimeMillis();
	   		KasusUji kasusuji = (KasusUji) kasusujinya.next(); 	
	   		System.out.println("kasus uji ke = " + kasusuji.kode_kasus);
	   		
		   	ArrayList<Double> matriksmin = new ArrayList<Double>();
		   	for (int j=0;j<jumlahkelas.jumlahKelas; j++){
					matriksmin.add(0.0);
			}
		   	
		   	double euclidian;		
			for (int i=0;i<jumlahgejala;i++){
		   		for (int j=0;j<jumlahkelas.jumlahKelas; j++){
		   			Gejala gejala = Gejala.findById((long)(i+1));
		   			Som bobotsom = Som.find("gejala=? and kelas=?", gejala, (j+1)).first();
		   			DetailKasusUji detkasusuji = DetailKasusUji.find("kasusuji=? and gejala=?", kasusuji, gejala).first();
		   			euclidian = Math.pow((bobotsom.bobot - detkasusuji.derajat.nilai),2);
		   			matriksmin.set(j, matriksmin.get(j)+euclidian);
		   		}
		   	}	
			int kelasterpilih = 0;
			for (int j=0;j<jumlahkelas.jumlahKelas; j++){
				if (nilaiminimum>matriksmin.get(j)){			
					nilaiminimum = matriksmin.get(j);
					kelasterpilih = j+1;
				}
			}
			nilaiminimum = 100;
		   	List listkasus = Kasus.find("kelas=?", kelasterpilih).fetch();
		   	nilaiminimum = 100;
		   	Iterator kasus = listkasus.iterator();
		   	while (kasus.hasNext()){
		   		Kasus k = (Kasus) kasus.next();
		   		if(maxusia<k.usia){
		   			maxusia = k.usia;
		   		}
		   		if(minusia>k.usia){
		   			minusia = k.usia;
		   		}		   	
		   	}
		   	
		   	Iterator kas = listkasus.iterator();    	
		   	while (kas.hasNext()){
		   		Kasus k = (Kasus) kas.next();
		   		
		   		double simlokalusia = simlokalusia(maxusia, minusia, k.usia, kasusuji.usia,1);
		   		double simlokalusiaminkowski = simlokalusia(maxusia, minusia, k.usia, kasusuji.usia,2);
		   		double simlokalgejala = 0;
		   		double simlokalgejalaminkowski = 0;
		   		double m11 = 0;
		   		double m00 = 0;
		   		double jst = 0;
		   		double jt = 0;
		   		
		   		if (k.usia>17 && kasusuji.usia>17){
	    			m11 = m11+1;
	    			m00 = 0;
	    		}else
	    			if(k.usia<17 && kasusuji.usia<17){
	    				m11 = 0;
	    				m00 = m00+1;
	    			}
	
		   		List listgejala = Gejala.findAll();
		   		Iterator gejala = listgejala.iterator();
		   		while (gejala.hasNext()){
		   			Gejala g = (Gejala) gejala.next();
		   			DetailKasus detailkasus =DetailKasus.find("kasus=? and gejala=?" , k , g).first();
		   			DetailKasusUji detailkasusuji =DetailKasusUji.find("kasusuji=? and gejala=?" , kasusuji, g).first();
		   			simlokalgejala = simlokalgejala + simlokalgejala(maxGejala,detailkasus.derajat.nilai , detailkasusuji.derajat.nilai,1);
		   			simlokalgejalaminkowski = simlokalgejalaminkowski + simlokalgejala(maxGejala,detailkasus.derajat.nilai , detailkasusuji.derajat.nilai,2);
		   			if (detailkasus.derajat.nilai!=0){
		   				jt = jt +1 ;
		   				if (detailkasusuji.derajat.nilai!=0 && detailkasus.derajat.nilai!=0){
		   					jst = jst +1;
		   					m11 = m11+1;
		   				}
		   			}else {
		   				if(detailkasusuji.derajat.nilai==0 && detailkasus.derajat.nilai==0){
		   					m00 = m00 + 1;
		   				}
		   			}		       		
		   		}
		   		
		   		double hasilnearest = (double) (((simlokalgejala + simlokalusia)/bobot)*(jst/jt))*100;
		   		double hasilminkowski = (double) Math.cbrt(((simlokalgejalaminkowski + simlokalusiaminkowski))/bobot)*(jst/jt)*100;
		   		double hasilsmc = (double) (((m11+m00)/bobot)*(jst/jt)*100);
		   		
		   		
		   		HasilUjiIndexNearest rn = new HasilUjiIndexNearest();
		   		rn.kasus = k;
		   		rn.kasusuji = kasusuji;
		   		rn.hasilnearest = hasilnearest;
		   		rn.save();
		   		
		   		HasilUjiIndexMinkowski rm = new HasilUjiIndexMinkowski();
		   		rm.kasus = k;
		   		rm.kasusuji = kasusuji;
		   		rm.hasilminkowski = hasilminkowski;
		   		rm.save();
		   		
		   		HasilUjiIndexSMC rsmc = new HasilUjiIndexSMC();
		   		rsmc.kasus = k;
		   		rsmc.kasusuji = kasusuji;
		   		rsmc.hasilsmc = hasilsmc;
		   		rsmc.save();
		   		
		   		jt = 0;
		   		jst= 0;
		   		m11 = 0;
		   		m00 = 0;
		   	}
		   	//parameter maximum Similarity
		   	double maxSimMD = 0;
		   	double maxSimNN = 0;
		   	double maxSimSMC = 0;
		   	
		   	//Hasil Uji Nearest
		   	HasilUjiIndexNearest resultNN = null;
		   	List listnearest = HasilUjiIndexNearest.find("kasusuji=?", kasusuji).fetch();
		   	Iterator rN = listnearest.iterator();
		   	while (rN.hasNext()){
		   		HasilUjiIndexNearest rNearest = (HasilUjiIndexNearest) rN.next();
		   		if(maxSimNN<rNearest.hasilnearest){
		   			maxSimNN = rNearest.hasilnearest;
		   			resultNN= rNearest;
		   		}
		   	}
		   		
		   	//Hasil Uji Minkowski
		   	HasilUjiIndexMinkowski resultMD = null;
		 	List listminkowski = HasilUjiIndexMinkowski.find("kasusuji=?", kasusuji).fetch();
		   	Iterator rM = listminkowski.iterator();
		   	while (rM.hasNext()){
		   		HasilUjiIndexMinkowski rMinkowski = (HasilUjiIndexMinkowski) rM.next();
		   		if(maxSimMD<rMinkowski.hasilminkowski){
		   			maxSimMD = rMinkowski.hasilminkowski;
		   			resultMD= rMinkowski;
		   		}	
		   	}
		   	//Hasil Uji SMC
		   	HasilUjiIndexSMC resultSMC = null;
		 	List listsmc = HasilUjiIndexSMC.find("kasusuji=?", kasusuji).fetch();
		   	Iterator rSMC = listsmc.iterator();
		   	while (rSMC.hasNext()){
		   		HasilUjiIndexSMC rSimpleMatch = (HasilUjiIndexSMC) rSMC.next();
		   		if(maxSimSMC<rSimpleMatch.hasilsmc){
		   			maxSimSMC = rSimpleMatch.hasilsmc;
		   			resultSMC= rSimpleMatch;
		   		} 	
		   	}
			listnearestneighbor.add(resultNN);
			listminkowskidistance.add(resultMD);
			listsimplematching.add(resultSMC);
			end = System.currentTimeMillis();
			waktu = (double) ((end-start)/1000.0);
		} 
   	
   	
		render (listnearestneighbor, listminkowskidistance, listsimplematching, userid, waktu);  	
  }
  
  public static void pengujiandoindexnn(){	   	
	   	String userid = " ";
	   	if(user!=null){
	   		userid =user.level.id.toString();
	   	}
	   	HasilUjiIndexNearest.deleteAll();
	    List<HasilUjiIndexNearest> listnearestneighbor = new ArrayList<HasilUjiIndexNearest>();
	    
	   	double waktu = 0;
	    long start;
	    long end;
	    int jumlahkasusuji = 0;
		int kasusbenar = 0;
	    
	   	//parameter
	   	int maxGejala = 3;
	   	double bobot = 34;
	   	int maxusia = 0;
	   	int minusia=100;
	   	double nilaiminimum = 100;
	   	int jumlahgejala = (int) Gejala.count();
	   	
		List listkasuss = Kasus.findAll();
		Iterator kasus = listkasuss.iterator();
	   	while (kasus.hasNext()){
	   		Kasus k = (Kasus) kasus.next();
	   		if(maxusia<k.usia){
	   			maxusia = k.usia;
	   		}
	   		if(minusia>k.usia){
	   			minusia = k.usia;
	   		}		   	
	   	}
	   	List listkasusuji = KasusUji.findAll();
	   	Iterator kasusujinya = listkasusuji.iterator();
	   	jumlahkasusuji = listkasusuji.size();
	   	
	   	while (kasusujinya.hasNext()){
	   		waktu = 0;
	   		start = System.currentTimeMillis();
		   		KasusUji kasusuji = (KasusUji) kasusujinya.next(); 	
		   		System.out.println("kasus uji ke = " + kasusuji.kode_kasus);
		   		
			   	ArrayList<Double> matriksmin = new ArrayList<Double>();
			   	for (int j=0;j<jumlahkelas.jumlahKelas; j++){
						matriksmin.add(0.0);
				}
			   	
			   	double euclidian;		
				for (int i=0;i<jumlahgejala;i++){
			   		for (int j=0;j<jumlahkelas.jumlahKelas; j++){
			   			Gejala gejala = Gejala.findById((long)(i+1));
			   			Som bobotsom = Som.find("gejala=? and kelas=?", gejala, (j+1)).first();
			   			DetailKasusUji detkasusuji = DetailKasusUji.find("kasusuji=? and gejala=?", kasusuji, gejala).first();
			   			euclidian = Math.pow((bobotsom.bobot - detkasusuji.derajat.nilai),2);
			   			matriksmin.set(j, matriksmin.get(j)+euclidian);
			   		}
			   	}	
				int kelasterpilih = 0;
				for (int j=0;j<jumlahkelas.jumlahKelas; j++){
					if (nilaiminimum>matriksmin.get(j)){			
						nilaiminimum = matriksmin.get(j);
						kelasterpilih = j+1;
					}
				}
				nilaiminimum = 100;
			   	List listkasus = Kasus.find("kelas=?", kelasterpilih).fetch();			   	
			   	Iterator kas = listkasus.iterator();    	
			   	while (kas.hasNext()){
			   		Kasus k = (Kasus) kas.next();
			   		
			   		double simlokalusia = simlokalusia(maxusia, minusia, k.usia, kasusuji.usia,1);
			   		double simlokalgejala = 0;
			   		double jst = 1;
			   		double jt = 1;
			   		
		
			   		List listgejala = Gejala.findAll();
			   		Iterator gejala = listgejala.iterator();
			   		while (gejala.hasNext()){
			   			Gejala g = (Gejala) gejala.next();
			   			DetailKasus detailkasus =DetailKasus.find("kasus=? and gejala=?" , k , g).first();
			   			DetailKasusUji detailkasusuji =DetailKasusUji.find("kasusuji=? and gejala=?" , kasusuji, g).first();
			   			simlokalgejala = simlokalgejala + simlokalgejala(maxGejala,detailkasus.derajat.nilai , detailkasusuji.derajat.nilai,1);
			   			if (detailkasusuji.derajat.nilai!=0){
			   				jt = jt +1 ;
			   			}
			   				if (detailkasusuji.derajat.nilai!=0 && detailkasus.derajat.nilai!=0){
			   					jst = jst +1;
			   				}       		
			   		}			   		
			   		double hasilnearest = (double) (((simlokalgejala + simlokalusia)/bobot)*(jst/jt))*100;	   		
			   		
			   		HasilUjiIndexNearest rn = new HasilUjiIndexNearest();
			   		rn.kasus = k;
			   		rn.kasusuji = kasusuji;
			   		rn.hasilnearest = hasilnearest;
			   		rn.save();
			   		
			   		jt = 1;
			   		jst= 1;
			   	}
			   	//parameter maximum Similarity
			   	double maxSimNN = 0;
			   	
			   	//Hasil Uji Nearest
			   	HasilUjiIndexNearest resultNN = null;
			   	List listnearest = HasilUjiIndexNearest.find("kasusuji=?", kasusuji).fetch();
			   	Iterator rN = listnearest.iterator();
			   	while (rN.hasNext()){
			   		HasilUjiIndexNearest rNearest = (HasilUjiIndexNearest) rN.next();
			   		if(maxSimNN<rNearest.hasilnearest){
			   			maxSimNN = rNearest.hasilnearest;
			   			resultNN= rNearest;
			   		}
			   	}			  
				listnearestneighbor.add(resultNN);
				end = System.currentTimeMillis();
				waktu = (double) (((end-start)/1000.0)/jumlahkasusuji);
				
				if(resultNN.kasusuji.penyakit == resultNN.kasus.penyakit){
					kasusbenar = kasusbenar + 1;
				}
			}   
			render (listnearestneighbor, userid, waktu, kasusbenar);  	
  }
  
  public static void pengujiandoindexmd(){
	   	HasilUjiIndexMinkowski.deleteAll();
	    List<HasilUjiIndexMinkowski> listminkowskidistance = new ArrayList<HasilUjiIndexMinkowski>();

	   	String userid = " ";
	   	if(user!=null){
	   		userid =user.level.id.toString();
	   	}
	   	int jumlahkasusuji = 0;
	   
	   	int kasusbenar = 0;
	   	double akurasi = 0;
	   	double waktu = 0;
	    long start;
	    long end;
	    
	   	//parameter
	   	int maxGejala = 3;
	   	double bobot = 34;
	   	int maxusia = 0;
	   	int minusia=100;
	   	double nilaiminimum = 100;
	   	int jumlahgejala = (int) Gejala.count();
	   	List listkasuss = Kasus.findAll();
		Iterator kasus = listkasuss.iterator();
	   	while (kasus.hasNext()){
	   		Kasus k = (Kasus) kasus.next();
	   		if(maxusia<k.usia){
	   			maxusia = k.usia;
	   		}
	   		if(minusia>k.usia){
	   			minusia = k.usia;
	   		}		   	
	   	}
	   	
	   	List listkasusuji = KasusUji.findAll();
		jumlahkasusuji = listkasusuji.size();
	   	Iterator kasusujinya = listkasusuji.iterator();
	   	while (kasusujinya.hasNext()){
	   		waktu = 0;
	   		start = System.currentTimeMillis();
		   		KasusUji kasusuji = (KasusUji) kasusujinya.next(); 	
		   		System.out.println("kasus uji ke = " + kasusuji.kode_kasus);
		   		
			   	ArrayList<Double> matriksmin = new ArrayList<Double>();
			   	for (int j=0;j<jumlahkelas.jumlahKelas; j++){
						matriksmin.add(0.0);
				}
			   	
			   	double euclidian;		
				for (int i=0;i<jumlahgejala;i++){
			   		for (int j=0;j<jumlahkelas.jumlahKelas; j++){
			   			Gejala gejala = Gejala.findById((long)(i+1));
			   			Som bobotsom = Som.find("gejala=? and kelas=?", gejala, (j+1)).first();
			   			DetailKasusUji detkasusuji = DetailKasusUji.find("kasusuji=? and gejala=?", kasusuji, gejala).first();
			   			euclidian = Math.pow((bobotsom.bobot - detkasusuji.derajat.nilai),2);
			   			matriksmin.set(j, matriksmin.get(j)+euclidian);
			   		}
			   	}	
				int kelasterpilih = 0;
				for (int j=0;j<jumlahkelas.jumlahKelas; j++){
					if (nilaiminimum>matriksmin.get(j)){			
						nilaiminimum = matriksmin.get(j);
						kelasterpilih = j+1;
					}
				}
				nilaiminimum = 100;
			   	List listkasus = Kasus.find("kelas=?", kelasterpilih).fetch();	   	
			   	Iterator kas = listkasus.iterator();    	
			   	while (kas.hasNext()){
			   		Kasus k = (Kasus) kas.next();
			   		
			   		double simlokalusiaminkowski = simlokalusia(maxusia, minusia, k.usia, kasusuji.usia,2);
			   		double simlokalgejalaminkowski = 0;
			   		double jst = 1;
			   		double jt = 1;
			   		
		
			   		List listgejala = Gejala.findAll();
			   		Iterator gejala = listgejala.iterator();
			   		while (gejala.hasNext()){
			   			Gejala g = (Gejala) gejala.next();
			   			DetailKasus detailkasus =DetailKasus.find("kasus=? and gejala=?" , k , g).first();
			   			DetailKasusUji detailkasusuji =DetailKasusUji.find("kasusuji=? and gejala=?" , kasusuji, g).first();
			   			simlokalgejalaminkowski = simlokalgejalaminkowski + simlokalgejala(maxGejala,detailkasus.derajat.nilai , detailkasusuji.derajat.nilai,2);
			   			if (detailkasusuji.derajat.nilai!=0){
			   				jt = jt +1 ;
			   				if (detailkasusuji.derajat.nilai!=0 && detailkasus.derajat.nilai!=0){
			   					jst = jst +1;
			   				}
			   			}	       		
			   		}			   		
			   		double hasilminkowski = (double) Math.cbrt(((simlokalgejalaminkowski + simlokalusiaminkowski))/bobot)*(jst/jt)*100;
			   		
			   		HasilUjiIndexMinkowski rm = new HasilUjiIndexMinkowski();
			   		rm.kasus = k;
			   		rm.kasusuji = kasusuji;
			   		rm.hasilminkowski = hasilminkowski;
			   		rm.save();
			   					   		
			   		jt = 1;
			   		jst= 1;

			   	}
			   	//parameter maximum Similarity
			   	double maxSimMD = 0;
			   		
			   	//Hasil Uji Minkowski
			   	HasilUjiIndexMinkowski resultMD = null;
			 	List listminkowski = HasilUjiIndexMinkowski.find("kasusuji=?", kasusuji).fetch();
			   	Iterator rM = listminkowski.iterator();
			   	while (rM.hasNext()){
			   		HasilUjiIndexMinkowski rMinkowski = (HasilUjiIndexMinkowski) rM.next();
			   		if(maxSimMD<rMinkowski.hasilminkowski){
			   			maxSimMD = rMinkowski.hasilminkowski;
			   			resultMD= rMinkowski;
			   		}
			   	}
				listminkowskidistance.add(resultMD);
				end = System.currentTimeMillis();
				waktu = (double) ((end-start)/1000.0)/jumlahkasusuji;	
				
				if(resultMD.kasusuji.penyakit == resultMD.kasus.penyakit){
					kasusbenar = kasusbenar + 1;
		   		}
		   			akurasi = (double)(kasusbenar/jumlahkasusuji)*100;
			}
		render (listminkowskidistance, userid, waktu, kasusbenar, akurasi);  
  }
  
  public static void pengujiandoindexsmc(){
	   	HasilUjiIndexSMC.deleteAll();
	    List<HasilUjiIndexSMC> listsimplematching = new ArrayList<HasilUjiIndexSMC>();
	   	String userid = " ";
	   	if(user!=null){
	   		userid =user.level.id.toString();
	   	}
	   	double waktu = 0;
	    long start;
	    long end;
	    int jumlahkasusuji = 0;
		int kasusbenar = 0;
	    
	   	//parameter
	   	double bobot = 34;
	   	double nilaiminimum = 100;
	   	int jumlahgejala = (int) Gejala.count();
	   	
	   	List listkasusuji = KasusUji.findAll();
	   	Iterator kasusujinya = listkasusuji.iterator();
	   	jumlahkasusuji = listkasusuji.size();
	   	
	   	while (kasusujinya.hasNext()){
	   		waktu = 0;
	   		start = System.currentTimeMillis();
		   		KasusUji kasusuji = (KasusUji) kasusujinya.next(); 	
		   		System.out.println("kasus uji ke = " + kasusuji.kode_kasus);
		   		
			   	ArrayList<Double> matriksmin = new ArrayList<Double>();
			   	for (int j=0;j<jumlahkelas.jumlahKelas; j++){
						matriksmin.add(0.0);
				}
			   	
			   	double euclidian;		
				for (int i=0;i<jumlahgejala;i++){
			   		for (int j=0;j<jumlahkelas.jumlahKelas; j++){
			   			Gejala gejala = Gejala.findById((long)(i+1));
			   			Som bobotsom = Som.find("gejala=? and kelas=?", gejala, (j+1)).first();
			   			DetailKasusUji detkasusuji = DetailKasusUji.find("kasusuji=? and gejala=?", kasusuji, gejala).first();
			   			euclidian = Math.pow((bobotsom.bobot - detkasusuji.derajat.nilai),2);
			   			matriksmin.set(j, matriksmin.get(j)+euclidian);
			   		}
			   	}	
				int kelasterpilih = 0;
				for (int j=0;j<jumlahkelas.jumlahKelas; j++){
					if (nilaiminimum>matriksmin.get(j)){			
						nilaiminimum = matriksmin.get(j);
						kelasterpilih = j+1;
					}
				}
				nilaiminimum = 100;
			   	List listkasus = Kasus.find("kelas=?", kelasterpilih).fetch();
			   	
			   	Iterator kas = listkasus.iterator();    	
			   	while (kas.hasNext()){
			   		Kasus k = (Kasus) kas.next();
			  
			   		double m11 = 0;
			   		double m00 = 0;
			   		double jst = 1;
			   		double jt = 1;
			   		
			   		if (k.usia>17 && kasusuji.usia>17){
		    			m11 = m11+1;
		    			m00 = 0;
		    		}else
		    			if(k.usia<17 && kasusuji.usia<17){
		    				m11 = 0;
		    				m00 = m00+1;
		    			}
		
			   		List listgejala = Gejala.findAll();
			   		Iterator gejala = listgejala.iterator();
			   		while (gejala.hasNext()){
			   			Gejala g = (Gejala) gejala.next();
			   			DetailKasus detailkasus =DetailKasus.find("kasus=? and gejala=?" , k , g).first();
			   			DetailKasusUji detailkasusuji =DetailKasusUji.find("kasusuji=? and gejala=?" , kasusuji, g).first();
			   			if (detailkasusuji.derajat.nilai!=0){
			   				jt = jt +1 ;
			   				if (detailkasusuji.derajat.nilai!=0 && detailkasus.derajat.nilai!=0){
			   					jst = jst +1;
			   					m11 = m11+1;
			   				}
			   			}else {
			   				if(detailkasusuji.derajat.nilai==0 && detailkasus.derajat.nilai==0){
			   					m00 = m00 + 1;
			   				}
			   			}		       		
			   		}
			   		double hasilsmc = (double) (((m11+m00)/bobot)*(jst/jt)*100);		   		
			   		HasilUjiIndexSMC rsmc = new HasilUjiIndexSMC();
			   		rsmc.kasus = k;
			   		rsmc.kasusuji = kasusuji;
			   		rsmc.hasilsmc = hasilsmc;
			   		rsmc.save();
			   		
			   		jt = 1;
			   		jst= 1;
			   		m11 = 0;
			   		m00 = 0;
			   	}
			   	//parameter maximum Similarity
			   	double maxSimSMC = 0;
			   	
			   	//Hasil Uji SMC
			   	HasilUjiIndexSMC resultSMC = null;
			 	List listsmc = HasilUjiIndexSMC.find("kasusuji=?", kasusuji).fetch();
			   	Iterator rSMC = listsmc.iterator();
			   	while (rSMC.hasNext()){
			   		HasilUjiIndexSMC rSimpleMatch = (HasilUjiIndexSMC) rSMC.next();
			   		if(maxSimSMC<rSimpleMatch.hasilsmc){
			   			maxSimSMC = rSimpleMatch.hasilsmc;
			   			resultSMC= rSimpleMatch;
			   		} 	
			   	}
				listsimplematching.add(resultSMC);
				end = System.currentTimeMillis();
				waktu = (double) (((end-start)/1000.0)/jumlahkasusuji);
				if(resultSMC.kasusuji.penyakit == resultSMC.kasus.penyakit){
					kasusbenar = kasusbenar + 1;
				}
			}    	
			render (listsimplematching, userid, waktu, kasusbenar);  
  }
  
  public static void editrevisi(long id){
	  String userid = " ";
	   	if(user!=null){
	   		userid =user.level.id.toString();
	   	}
	   	Revisi objek;
		objek = Revisi.find("id=?", id).first();
    	if
    	(objek == null){
    		objek = new Revisi();
    	}
    	List listpenyakit = Penyakit.findAll();
    	render (objek, userid, listpenyakit); 
  }
  
   
   public static void detailkasusuji(long id){
	   String userid = " ";
	   if(user!=null){
   			userid =user.level.id.toString();
	   }
	   List listdetailkasusuji = DetailKasusUji.find("kasusuji_id=?", id).fetch();
	   	KasusUji objekK;
	   	objekK = KasusUji.findById(id);
	  	render (listdetailkasusuji, objekK, userid);
	}
   
   
   public static void editkasus(long id){
	   String userid = " ";
   	if(user!=null){
   		userid =user.level.id.toString();
   	}
   	Kasus objekK;
   	objekK = Kasus.find("id=?", id).first();
   	if
   	(objekK == null){

   		objekK = new Kasus();
   	}
   	List listpenyakit = Penyakit.findAll();
   	List listderajat = DerajatGejala.findAll();
    int jmlhgejala = (int) Gejala.count();
   	ArrayList<DetailKasus> listdetailkasus = new ArrayList<DetailKasus>();
//   	System.out.print(jmlhgejala);
   	for(int i=0;i<jmlhgejala;i++){
   		DetailKasus newdetailkasus = new DetailKasus();
   		newdetailkasus.gejala = Gejala.findById((long) i+1);
   		listdetailkasus.add(newdetailkasus);  		
   	}
   	System.out.print("jumlahnya " + listdetailkasus.size());
	System.out.print("listnya " + listdetailkasus);
   	
     	
   	render (objekK, listpenyakit, listderajat,listdetailkasus, userid);
   }
   public static void simpanK(@Valid Kasus objekK,ArrayList<DetailKasus> listdetailkasus){
   	Kasus g;
   	g = objekK;
   	if(validation.hasErrors()){
   		params.flash();
   		validation.keep();
   		editkasus(g.id);
   	}
   	g.save();
    
   	tambahdetail(g.id);
   }
   
   public static void hapuskasus(long id){
	DetailKasus.delete("id=?", id);
   	kasus ();
   }
   
   public static void hapuskasus1(long id){
		Kasus.delete("id=?", id);
	   	kasus ();
	   }
   public static void hapusrevisi (long id){
	   Revisi.delete("id=?", id);
	   revisi();
   }
   public static void hapusdetailrevisi (long id){
	   DetailRevisi.delete("id=?", id);
	   revisi();
   }


public static void tambahdetail(long id){
	String userid = " ";
   	if(user!=null){
   		userid =user.level.id.toString();
   	}
  	List listderajat = DerajatGejala.findAll();
   	int jmlhgejala = (int) Gejala.count();
   	ArrayList<DetailKasus> listdetailkasus = new ArrayList<DetailKasus>();
   	System.out.print(jmlhgejala);
   	for(int i=0;i<jmlhgejala;i++){
   		DetailKasus newdetailkasus = new DetailKasus();
   		newdetailkasus.gejala = Gejala.findById((long) i+1);
   		newdetailkasus.kasus = Kasus.findById(id);
   		listdetailkasus.add(newdetailkasus);
   	}
	 render (listderajat, listdetailkasus, id, userid);
	   }
 
   public static void simpanD(ArrayList gejala, ArrayList derajat, Long kasus){
	   for(int i=0; i<gejala.size(); i++){
		   DetailKasus dKasus = new DetailKasus();
		   DerajatGejala derajat_tmp = DerajatGejala.findById(Long.valueOf((String) derajat.get(i)));
		   Gejala gejala_tmp = Gejala.findById(Long.valueOf((String) gejala.get(i)));
		   Kasus kasus_tmp = Kasus.findById(kasus);
		   
		   dKasus.derajat = derajat_tmp;
		   dKasus.gejala = gejala_tmp;
		   dKasus.kasus = kasus_tmp;
		   dKasus.save();
	   }
	   
	   detailkasus(kasus);
   }	   

public static void detailkasus(long id){
	   String userid = " ";
	   if(user!=null){
   			userid =user.level.id.toString();
	   }
	   List listdetailkasus = DetailKasus.find("kasus_id=?", id).fetch();
	   	Kasus objekK;
	   	objekK = Kasus.findById(id);
	  	render (listdetailkasus, objekK, userid);
	   }
public static void detailrevisi(long id){
	 String userid = " ";
	   if(user!=null){
 			userid =user.level.id.toString();
	   }
	   List listdetailrevisi = DetailRevisi.find("revisi_id=?", id).fetch();
	   Revisi objek;
	   objek = Revisi.findById(id);
	  	render (listdetailrevisi, objek, userid);
	}
public static void validasi(){
	 String userid = " ";
	   if(user!=null){
 			userid =user.level.id.toString();
	   }
	 int jumlahkelas = 0;
	 int kelasnya = 0;
	 int jumlahgejala = (int) Gejala.count();
	 double validasi = 0;
	 double validasi1=0;
	 double jumlahakarvalidasi = 0;
	 double alfasilhouette = 0;
	 double betasilhouette = 0;
	 double pembagi = 0;
	 double akarvalidasi = 0;
	 double nilaiminimum = 100;
	 double silhouette = 0;

		
	 List listkelas = KelasSom.findAll();
	 Iterator kelas = listkelas.iterator();
	 while (kelas.hasNext()){
		 KelasSom kls = (KelasSom) kelas.next();
		 jumlahkelas = kls.jumlahKelas;
	 }
	 System.out.println("Jumlah Kelas = " + jumlahkelas);
	 
	 List listkasus = Kasus.findAll();
	 Iterator kasus = listkasus.iterator();
	 while (kasus.hasNext()){
			 Kasus k = (Kasus) kasus.next();
			 System.out.println("Kode Kasus :" + k.kode_kasus);
			 kelasnya = k.kelas;
			 System.out.println("kelasnya " + kelasnya);
			 
			 List listkasusnya = Kasus.findAll();
			 Iterator kasusnya = listkasusnya.iterator();
				 while (kasusnya.hasNext()){
					 Kasus kk = (Kasus) kasusnya.next();
					 if (kelasnya==kk.kelas){
						 pembagi = pembagi + 1;
						 for (int g=0; g<jumlahgejala; g++){
							 Gejala gejala = Gejala.findById((long)(g+1));
							 DetailKasus detailkasus = DetailKasus.find("kasus=? and gejala=?", k, gejala).first();
							 DetailKasus detailkasuss = DetailKasus.find("kasus=? and gejala=?", kk, gejala).first(); 
							 validasi = Math.pow((detailkasus.derajat.nilai - detailkasuss.derajat.nilai),2);
							 validasi1 = validasi1 + validasi;
						 }
						 akarvalidasi = Math.sqrt(validasi1);
						 jumlahakarvalidasi = jumlahakarvalidasi + akarvalidasi;
						 validasi1 = 0;
						 validasi = 0;	
					 }
				 }	
				 pembagi = pembagi - 1;
				 if (pembagi == 0){
					 pembagi = 1;
				 }
				 alfasilhouette = (jumlahakarvalidasi/pembagi);
				 System.out.println("Pembaginya= " + pembagi);
				 System.out.println("Nilai Silhoutte Alfa= " + alfasilhouette);		 
				 				 
				 k.alfasilhoutte = alfasilhouette;
				 k.save();
				 
				 akarvalidasi = 0;
				 jumlahakarvalidasi = 0;
				 pembagi = 0;
				 kelasnya = 0;
	 ArrayList<Double> minbetasilhouette = new ArrayList<Double>();
	 for (int i=0; i<=jumlahkelas; i++){
		 minbetasilhouette.add(0.0);
	 }
	 kelasnya = k.kelas;
		 for (int i=0; i<=jumlahkelas; i++){
			 System.out.println("Kelas ke - " + i);
			 List listkasusbeta1 = Kasus.findAll();
			 Iterator kasusbeta1 = listkasusbeta1.iterator();
			 while (kasusbeta1.hasNext()){
				 Kasus kBeta1 = (Kasus) kasusbeta1.next();
				 if (i == kBeta1.kelas){
					 pembagi = pembagi + 1;					 
					 for (int g=0; g<jumlahgejala; g++){
						 Gejala gejala = Gejala.findById((long)(g+1));
						 DetailKasus detailkasus = DetailKasus.find("kasus=? and gejala=?", k, gejala).first();
						 DetailKasus detailkasuss = DetailKasus.find("kasus=? and gejala=?", kBeta1, gejala).first(); 
						 validasi = Math.pow((detailkasus.derajat.nilai - detailkasuss.derajat.nilai),2);
						 validasi1 = validasi1 + validasi;
					 }
					 akarvalidasi = Math.sqrt(validasi1);
					 jumlahakarvalidasi = jumlahakarvalidasi + akarvalidasi;
					 validasi1 = 0;
					 validasi = 0;	
				 }
				 if ( i == 0){
					 jumlahakarvalidasi = 20;
					 pembagi = 2;
				 }
				 if (i == k.kelas){
					 jumlahakarvalidasi = 20;
					 pembagi = 2;
					 
				 }
			 }
			 pembagi = pembagi - 1;
			 if (pembagi == 0){
				 pembagi = 1;
			 }
			 betasilhouette = (jumlahakarvalidasi/pembagi);
			 minbetasilhouette.set(i,minbetasilhouette.get(i)+ betasilhouette);
			 System.out.println("Nilai Silhoutte Beta= " + betasilhouette);
			 System.out.println("Array beta = " + minbetasilhouette);
			 akarvalidasi = 0;
			 jumlahakarvalidasi = 0;
			 pembagi = 0;
			 kelasnya = 0;
		 }
		for (int i=0; i<=jumlahkelas; i++){
			if (nilaiminimum>minbetasilhouette.get(i)){
				nilaiminimum = minbetasilhouette.get(i);
			}
		}
		System.out.println("Nilai minimum = " + nilaiminimum);
		silhouette = (double)((nilaiminimum - k.alfasilhoutte)/(Math.max(nilaiminimum, k.alfasilhoutte)));
		nilaiminimum = 100;
		System.out.println("Nilai Silhouette kasus : " + k.kode_kasus + " " + "adalah " + silhouette);
		
		k.nilaisilhouette = silhouette;
		k.save();
	 }
	 double nilaisilhouette = 0;
	   double jumlahkasus = Kasus.count();
	   double silhouetteglobal;
	   List listkasusnya = Kasus.findAll();
	   Iterator kasusnya = listkasusnya.iterator();
		 while (kasusnya.hasNext()){
				 Kasus k = (Kasus) kasusnya.next();
				 nilaisilhouette = nilaisilhouette + k.nilaisilhouette;
				 System.out.println("Kasus ke : " + k.kode_kasus);
				 System.out.println("Nilai silhouette lokal = " + k.nilaisilhouette);
		 }
		 silhouetteglobal = (double)(nilaisilhouette/jumlahkasus);
	 List allkasus = Kasus.findAll();
	 render (userid, allkasus, silhouetteglobal);
	 }
public static void silhouette(){
	String userid = " ";
	   if(user!=null){
			userid =user.level.id.toString();
	   }
	   int jumlahkelas=0;
	   double jumlahkasus = 0;
	   double silhouetteglobal =0;;
	   double silhouettelokal = 0;
	   double nilaisilhouette = 0;
	   double sc= 0;
	   
	   List listkelas = KelasSom.findAll();
		 Iterator kelas = listkelas.iterator();
		 while (kelas.hasNext()){
			 KelasSom kls = (KelasSom) kelas.next();
			 jumlahkelas = kls.jumlahKelas;
		 }
		 System.out.println("Jumlah Kelas = " + jumlahkelas);
		 
	
	for (int i=1; i<=jumlahkelas; i++){
	   List listkasus = Kasus.findAll();
	   Iterator kasus = listkasus.iterator();
		 while (kasus.hasNext()){
				 Kasus k = (Kasus) kasus.next();
				 if (i == k.kelas){
					 jumlahkasus = jumlahkasus + 1;
					 silhouettelokal = silhouettelokal + k.nilaisilhouette; 
				 }
		 }
		 silhouetteglobal = (silhouettelokal/jumlahkasus);
		 nilaisilhouette = nilaisilhouette + silhouetteglobal;
		 System.out.println("Kelas ke - " + i);
		 System.out.println("Jumlah Kasus = " + jumlahkasus);
		 System.out.println("Silhouette lokal = " + silhouettelokal);
		 System.out.println("Nilai Silhouette kelas " + i + "adalah " + silhouetteglobal);
		 silhouetteglobal = 0;
		 silhouettelokal = 0;
		 jumlahkasus = 0;
		 
	}
	sc = nilaisilhouette /jumlahkelas;
	System.out.println("SC = " + sc);
		
	render (userid, silhouetteglobal, sc);
}

		 
} 
