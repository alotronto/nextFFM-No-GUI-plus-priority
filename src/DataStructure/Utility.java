package DataStructure;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import StructureClass.SolutionInOrder;
import StructureClass.Squadra;
import StructureClass.Intervento;


public class Utility {

	
	//Attributi della classe per la gestione della apertura, scrittura e chiusura
	//di un file
	private static FileWriter myWriter;
	private static BufferedWriter myBufferWriter;
	
	//Attributi della classe per la memorizzazione delle squadre, interventi
	public static ArrayList<Intervento> interventi = new ArrayList<Intervento>();
	//public static HashMap<String, String> interventi = new HashMap<String, String>();
	public static ArrayList<Squadra> squadre = new ArrayList<Squadra>();
	
	//Attributi per il calcolo del costo delle soluzioni
	public static ArrayList<Map<String, String>> distanzeImpianti = new ArrayList<Map<String, String>>();
	public static HashMap<String, String[]> distanzeImpiantiNew = new HashMap<String, String[]>();
	private static float costoBenzina;
	private static float costoDiesel;
	private static String idSedediPartenza="0";
	
	
	//public static Random random = new Random();
	public static SecureRandom sRandom = new SecureRandom();
	
	
	public static SolutionInOrder initialSolution = new SolutionInOrder(4);
	public static SolutionInOrder valuateSolution = new SolutionInOrder(4);
	public static ArrayList<Intervento> interventiNonAssegnati;
	
	// ----Parametri necessari per la modifica della soluzione finale
	private static int giornataLavorativa = 480;
	
	
	//--Parametri per i TEST
	//public static TreeMap<Double,Integer> risultati  = new TreeMap<Double, Integer>();
	private static ArrayList<Float> risultati = new ArrayList<Float>();
	/**
	 * Metodo per l'inizializzazione del costo dei carburanti.
	 */
	public static void initCostoCarguranti(){
		costoBenzina = DataBaseUtility.Utility.getCostoBenzinaCarburanti();
		costoDiesel = DataBaseUtility.Utility.getCostoDieselCarburanti();
	}
	
	/**
	 * Metodo per l'inizializzazione dell'oggetto Map per la memorizzazione delle
	 * distanze
	 */
	public static void initMapSpostamenti(){
		distanzeImpianti = DataBaseUtility.Utility.getMapSpostamenti();
	}
	
	/**
	 * Metodo per l'inizializzazione dell'oggetto Map per la memorizzazione delle
	 * distanze
	 */
	public static void initMapSpostamentiNewVersion(){
		distanzeImpiantiNew = DataBaseUtility.Utility.getMapSpostamentiNewVersion();
	}
	
	/**
	 * Metdo per l'estrazione di  tutte le squadre dal DB;
	 * Per ogni Squadra esistente calcolo le competenze totali della squadra;
	 * Per ogni Squadra viene calcolato il costo orario
	 * Per ogni squadra inizializzo il tipo di automezzo ed il suo consumo.
	 * Vengono inoltre inizializzati (ALL'INTERNO DI QUESTO METODO PER COMODITA') il costo dei carburanti
	 * e una mappa con le distenze e i tempi di percorrenza tra le varie sedi 
	 */
	public static void initSquadre(){
		
		//Estrazione delle squadre da DB
		ResultSet myResutlSet = DataBaseUtility.Utility.getSquadre();
		try {
			while (myResutlSet.next()) {
				squadre.add(new Squadra(myResutlSet.getString("idSquadra")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		//Competenze per ogni squadra
		for(int i=0;i<squadre.size();i++){
			myResutlSet = DataBaseUtility.Utility.getCopetenzethatSquadra(squadre.get(i).getId());
			try{
				while(myResutlSet.next()){
					squadre.get(i).addCompentenza(myResutlSet.getString("idCompetenza"));
				}
			}catch (SQLException e) {
				e.printStackTrace();
				System.out.println(e.getMessage());
			}
		}
		
		//Costo orario squadra
		for(int i=0;i<squadre.size();i++){
			squadre.get(i).setCostoOrarioSquadra(DataBaseUtility.Utility.getCostoOrarioSquadra(squadre.get(i).getId()));
		}
		
		//Inizializzo il tipo di automezzo ed il suo consumo e la sua usura
		try{
			myResutlSet = DataBaseUtility.Utility.initAutomezzoSquadre();
			
			for(int i=0;(i<squadre.size() && myResutlSet.next());i++){
				squadre.get(i).setAlimentazioneAuto(myResutlSet.getString("alimentazione"));
				squadre.get(i).setConsumo(myResutlSet.getFloat("consumo"));
				squadre.get(i).setCostoProporzionale(myResutlSet.getFloat("costiProporzionali"));
			}
		}catch (SQLException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		DataBaseUtility.Utility.dbCloseConnection();
		
	}
	
	/**
	 * Metodo per l'estrazione di  tutti gli interventi dal DB e dell'inizializzazione dei valori di 
	 * durata e idImpianto dei singoli interventi
	 */
	public static void initInterventi(){
		ResultSet myResutlSet = DataBaseUtility.Utility.getInterventi();
		try{
			while(myResutlSet.next()){
				interventi.add(new Intervento(myResutlSet.getString("idIntervento")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		
		myResutlSet = DataBaseUtility.Utility.getInfoInterventi();
		try{
			for(int i=0; i<interventi.size() && myResutlSet.next(); i++){
				interventi.get(i).setPriority(myResutlSet.getInt("priorita"));
				interventi.get(i).setDurata(myResutlSet.getInt("durata"));
				interventi.get(i).setIdImpianto(myResutlSet.getString("idImpianto"));	
			}
			
			//Competenze necessarie per ogni intervento
			for(int i=0;i<interventi.size();i++){
				myResutlSet = DataBaseUtility.Utility.getCopetenzeIntervento(interventi.get(i).getId());
				while(myResutlSet.next()){
					interventi.get(i).setCompentenza(myResutlSet.getString("idCompetenza"));
				}
			}
				
		}catch (SQLException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		DataBaseUtility.Utility.dbCloseConnection();
	}
	
	
	
	/**
	 * Metodo di supporto per l'inizializzazione degli interventi da utilizzare
	 * nell'algoritmo.  Il metodo preleverà in modo random @param number
	 * di interventi dai totali presenti nel DB utilizzato
	 * 
	 * @param number
	 */
	public static void initInterventiRandom(int numberPA, int numberCA, int numberME) {
		// Aggiornamento per la versione random e con numero di elementi

		// ***** Prelevo tutti gli interventi dal DB
		ArrayList<Intervento> totaleInterventi = new ArrayList<Intervento>();
		ResultSet myResutlSet = DataBaseUtility.Utility.getInterventi();
		
		try{
			while(myResutlSet.next()){
				totaleInterventi.add(new Intervento(myResutlSet.getString("idIntervento")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		
		myResutlSet = DataBaseUtility.Utility.getInfoInterventi();
		try{
			for(int i=0; i<totaleInterventi.size() && myResutlSet.next(); i++){
				totaleInterventi.get(i).setPriority(myResutlSet.getInt("priorita"));
				totaleInterventi.get(i).setDurata(myResutlSet.getInt("durata"));
				totaleInterventi.get(i).setIdImpianto(myResutlSet.getString("idImpianto"));	
			}
			
			//Competenze necessarie per ogni intervento
			for(int i=0;i<totaleInterventi.size();i++){
				myResutlSet = DataBaseUtility.Utility.getCopetenzeIntervento(totaleInterventi.get(i).getId());
				while(myResutlSet.next()){
					totaleInterventi.get(i).setCompentenza(myResutlSet.getString("idCompetenza"));
				}
			}
				
		}catch (SQLException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		DataBaseUtility.Utility.dbCloseConnection();
		
		// ****************************************
		int[] selected = new int[numberPA];
		// Prelevo solo quelli Custom e in modo Random in provincia di PA
		for (int i = 0; i < numberPA; i++) {
			int interventoRandom;
			if (i == 0) {
				interventoRandom = sRandom.nextInt(20);
				selected[i] = interventoRandom;
				interventi.add(totaleInterventi.get(interventoRandom));
			} else {
				do {
					interventoRandom = sRandom.nextInt(20);
				} while (checkInVector(selected, interventoRandom));
				selected[i] = interventoRandom;
				interventi.add(totaleInterventi.get(interventoRandom));
			}
		}

		selected = new int[numberCA];
		// Prelevo solo quelli Custom e in modo Random in provincia di PA
		for (int i = 0; i < numberCA; i++) {
			int interventoRandom;

			if (i == 0) {
				interventoRandom = sRandom.nextInt(20);
				interventoRandom += 20;
				selected[i] = interventoRandom;
				interventi.add(totaleInterventi.get(interventoRandom));
			} else {
				do {
					interventoRandom = sRandom.nextInt(20);
					interventoRandom += 20;
				} while (checkInVector(selected, interventoRandom));
				selected[i] = interventoRandom;
				interventi.add(totaleInterventi.get(interventoRandom));
			}
		}

		selected = new int[numberME];
		// Prelevo solo quelli Custom e in modo Random in provincia di PA
		for (int i = 0; i < numberME; i++) {
			int interventoRandom;

			if (i == 0) {
				interventoRandom = sRandom.nextInt(20);
				interventoRandom += 40;
				selected[i] = interventoRandom;
				interventi.add(totaleInterventi.get(interventoRandom));
			} else {
				do {
					interventoRandom = sRandom.nextInt(20);
					interventoRandom += 40;
				} while (checkInVector(selected, interventoRandom));
				selected[i] = interventoRandom;
				interventi.add(totaleInterventi.get(interventoRandom));
			}
		}
	}
	

	
	/**
	 * Metodo di supporto per l'inizializzazione degli interventi da utilizzare
	 * nell'algoritmo.
	 * Il metodo preleverà solo gli interventi con ID contenuti in @param listOfID
	 * dai totali presenti nel DB utilizzato
	 */
	public static void initInterventiFromList(int[] listOfID) {
		// Aggiornamento per la versione random e con numero di elementi

		// ***** Prelevo tutti gli interventi dal DB
		ArrayList<Intervento> totaleInterventi = new ArrayList<Intervento>();
		ResultSet myResutlSet = DataBaseUtility.Utility.getInterventi();
		
		try{
			while(myResutlSet.next()){
				totaleInterventi.add(new Intervento(myResutlSet.getString("idIntervento")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		
		myResutlSet = DataBaseUtility.Utility.getInfoInterventi();
		try{
			for(int i=0; i<totaleInterventi.size() && myResutlSet.next(); i++){
				totaleInterventi.get(i).setPriority(myResutlSet.getInt("priorita"));
				totaleInterventi.get(i).setDurata(myResutlSet.getInt("durata"));
				totaleInterventi.get(i).setIdImpianto(myResutlSet.getString("idImpianto"));	
			}
			
			//Competenze necessarie per ogni intervento
			for(int i=0;i<totaleInterventi.size();i++){
				myResutlSet = DataBaseUtility.Utility.getCopetenzeIntervento(totaleInterventi.get(i).getId());
				while(myResutlSet.next()){
					totaleInterventi.get(i).setCompentenza(myResutlSet.getString("idCompetenza"));
				}
			}
				
		}catch (SQLException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		DataBaseUtility.Utility.dbCloseConnection();
		// ****************************************
		
		for(int i=0; i<listOfID.length;i++){
			int index = getIndexFromId_Intervention(listOfID[i],totaleInterventi);
			interventi.add(totaleInterventi.get(index));		
		}
	}
	
	
	/**
	 * Metdo di support per verifica se nel vettore @param vect è presente un elemento con valore
	 * @param key
	 * Il Metodo ritorna true se l'emento è stato trovato
	 * @return
	 */
	public static boolean checkInVector(int[] vect, int key){
		boolean result = false;
		for(int i = 0; i< vect.length; i++)
			if(vect[i]==key)
				result=true;
		return result;
	}
	
	/**
	 * Metodo di supporto per ricavare la distanza tra due impiantia
	 * @param idOrigine id dell'impianto di partenza
	 * @param idDestinazione id dell'impinato di destinazione
	 * @return double della distanza tra i due impinati
	 */
	/*
	public static float getDistanzaSpostamentiImpinatiHash(String idOrigine, String idDestinazione){
		
		float distanza=0;
		
		for(int i=0;i<distanzeImpianti.size();i++){
			
			if((distanzeImpianti.get(i).get("idImpianto1").equals(idOrigine)) && (distanzeImpianti.get(i).get("idImpianto2").equals(idDestinazione))){
				distanza= Float.valueOf(distanzeImpianti.get(i).get("distanza"));
				return distanza;
			}
			if((distanzeImpianti.get(i).get("idImpianto2").equals(idOrigine)) && (distanzeImpianti.get(i).get("idImpianto1").equals(idDestinazione))){
				distanza= Float.valueOf(distanzeImpianti.get(i).get("distanza"));
				return distanza;
			}
		}
		
		return distanza;
	}
	*/
	/**
	 * Metodo di supporto per ricavare la distanza tra due impiantia
	 * @param idOrigine id dell'impianto di partenza
	 * @param idDestinazione id dell'impinato di destinazione
	 * @return double della distanza tra i due impinati
	 */
	public static float getDistanzaSpostamentiImpinatiHashNew(String idOrigine, String idDestinazione){
		
		float distanza=0;
		
		String key1 = idOrigine+"-"+idDestinazione;
		String key2 = idDestinazione+"-"+idOrigine;
		String[] result = distanzeImpiantiNew.get(key1);
		if(result==null)
			result = distanzeImpiantiNew.get(key2);
			
		distanza = Float.valueOf(result[0]);
		
		return distanza;
	}
	
	/**
	 * Metodo di supporto per ricavare il tempo di spostamento tra due impianti
	 * @param idOrigine id dell'impianto di partenza
	 * @param idDestinazione id dell'impinato di destinazione
	 * @return double della tempo di percorrenza
	 */
	/*public static float getDurataSpostamentiImpinatiHash(String idOrigine, String idDestinazione){
		
		float durata=0;
		
		for(int i=0;i<distanzeImpianti.size();i++){
			
			if((distanzeImpianti.get(i).get("idImpianto1").equals(idOrigine)) && (distanzeImpianti.get(i).get("idImpianto2").equals(idDestinazione))){
				durata= Float.valueOf(distanzeImpianti.get(i).get("durata"));
				return durata;
			}
			if((distanzeImpianti.get(i).get("idImpianto2").equals(idOrigine)) && (distanzeImpianti.get(i).get("idImpianto1").equals(idDestinazione))){
				durata= Float.valueOf(distanzeImpianti.get(i).get("durata"));
				return durata;
			}
		}
		
		return durata;
	}*/
	
	/**
	 * Metodo di supporto per ricavare la distanza tra due impiantia
	 * @param idOrigine id dell'impianto di partenza
	 * @param idDestinazione id dell'impinato di destinazione
	 * @return double della distanza tra i due impinati
	 */
	public static float getDurataSpostamentiImpinatiHashNew(String idOrigine, String idDestinazione){
		
		float durata=0;
		
		String key1 = idOrigine+"-"+idDestinazione;
		String key2 = idDestinazione+"-"+idOrigine;
		String[] result = distanzeImpiantiNew.get(key1);
		if(result==null)
			result = distanzeImpiantiNew.get(key2);
			
		durata = Float.valueOf(result[1]);
		
		return durata;
	}
	
	/**
	 * Metodo per la determinazione del costo CONSIDERANDO l'ordine di esecuzione di una lista di interventi 
	 * affidati a una squadra
	 * 
	 * @param listaInterventi array di interi rappresentati gli id degli interventi
	 * @param idSquadra String rappresentante l'id della squadra
	 * @return costo di esecuzione
	 */
	public static float costoIntervetiPerSquadra(int[] listaInterventi,String idSquadra){
		float costoTotale;
		//float minutiGiornataLavorativa=480;
		float minutiLavoroNormale;
		float minutiLavoroStraordinario;
		float costoOrarioNormale= 0;
		float costoOrarioStraordinario= 0;
		float ore;
		//double costoUsuraAutomezzo=0.0;
		
		float consumoAutomezzo=0;
		float distanzaTotale=0;
		float durataSpostamenti=0;
		float durataInterventi=0;
		float consumoCarburanteTotale=0;
		float durataGiornataTotale=0;
		String idImpiantoAttuale = idSedediPartenza;
		String idImpiantoSuccessivo;
		
		//Setting del consumo dell'automezzo
		
		consumoAutomezzo = squadre.get(Integer.valueOf(idSquadra)).getConsumo();
		
		//Per il calco del costo necessito di calcolare la lunghezza degli spostamenti e la durata
		
		//Calcolo la distanza totale per visitare tutti gli impianti della lista di interventi
		for(int i=0;i<listaInterventi.length;i++){
			//CONTROLLARE QUESTA ISTRUZIONE
			int indice = getIndexFromId_Intervention(listaInterventi[i]); //Controllare
			
			idImpiantoSuccessivo = interventi.get(indice).getIdImpianto();
			
			if(! (idImpiantoAttuale.equals(idImpiantoSuccessivo)) ){
				//AGGIORNO LA DISTANZA TOTALE PERCORSA
				
				distanzaTotale+=getDistanzaSpostamentiImpinatiHashNew(idImpiantoAttuale, idImpiantoSuccessivo);
				//distanzaTotale+=getDistanzaImpianti(idImpiantoAttuale, idImpiantoSuccessivo);
				
				//AGGIORNO IL TEMPO TOTALE DEGLI SPOSTAMENTI
				//durataSpostamenti+=getDurataSpostamentiImpinati(idImpiantoAttuale, idImpiantoSuccessivo);
				durataSpostamenti+=getDurataSpostamentiImpinatiHashNew(idImpiantoAttuale, idImpiantoSuccessivo);
				
				//AGGIORNO IL TEMPO TOTALE DI INTERVENTI
				durataInterventi+=interventi.get(indice).getDurata();
				
				//IMPOSTO AGGIORNO L'IMPIANTO ATTUALE
				idImpiantoAttuale = idImpiantoSuccessivo;
			}
			else{
				durataInterventi+=interventi.get(indice).getDurata();
			}
			
		}
		
		//AGGIUNGO ALLA DISTANZA PERCORSA E AL TEMPO DI SPOSTAMENTO IL RITORNO ALLA SEDE DI PARTENZA
		//SE NON SONO GIA' NELLA SEDE DI PARTENZA
		if(! (idImpiantoAttuale.equals(idSedediPartenza)) ){
			
			distanzaTotale+=getDistanzaSpostamentiImpinatiHashNew(idImpiantoAttuale, idSedediPartenza);
			durataSpostamenti+=getDurataSpostamentiImpinatiHashNew(idImpiantoAttuale, idSedediPartenza);
			//distanzaTotale+=getDistanzaImpianti(idImpiantoAttuale, idSedediPartenza);
			//durataSpostamenti+=getDurataSpostamentiImpinati(idImpiantoAttuale, idSedediPartenza);
		}
		
		//RICAVO IL CONSUMO DI CARBURANTE & E I MINUTI PER ESEGUIRE INTERVENTI E SPOSTAMENTI
		consumoCarburanteTotale = (consumoAutomezzo * distanzaTotale / 100);
		durataGiornataTotale = durataSpostamenti + durataInterventi;
		
		
		//Ricavo costo in termini di euro
		
		//Determino se e' presente del lavoro da svolgere in tariffa di straordinario
		minutiLavoroStraordinario = durataGiornataTotale - giornataLavorativa;//minutiGiornataLavorativa;
		if(minutiLavoroStraordinario > 0){
			minutiLavoroNormale = giornataLavorativa;//minutiGiornataLavorativa;
		}
		else{
			minutiLavoroNormale = durataGiornataTotale;
		}
		
		//Calcolo il costo del lavoro della squadra in tariffa normale
		costoOrarioNormale =squadre.get(Integer.valueOf(idSquadra)).getCostoOrarioSquadra();
		ore = minutiLavoroNormale / 60;
		costoOrarioNormale *= ore;
		
		float costoConsumo = 0;
		
		//Controllo se la lista di interventi della squadra ha interventi o meno da fare
		// e  in caso positivo calcolo il costo consumo totale dell'automezzo
		
		//CALCOLO IL CONSUMO TOTALE TRA CARBURANTE E COSTO PROPORZIONALE
		//if(listaInterventi.length > 0 && consumoCarburanteTotale != 0){
			if(squadre.get(Integer.valueOf(idSquadra)).getAlimentazioneAutomezzo().equals("Benzina")){
				costoConsumo = (consumoCarburanteTotale * costoBenzina) + (squadre.get(Integer.valueOf(idSquadra)).getCostoProporzionale());
			}
			else{
				costoConsumo = (consumoCarburanteTotale) * costoDiesel + (squadre.get(Integer.valueOf(idSquadra)).getCostoProporzionale());
				
			}
		//}
		//else{
		//	costoConsumo = 0;
		//}
		
		//Calcolo il costo del lavoro della squadra in tariffa straordinaria
		if(minutiLavoroStraordinario>0){
			//Fattore di moltiplicazione per il costo del lavoro straordinario
			costoOrarioStraordinario = (squadre.get(Integer.valueOf(idSquadra)).getCostoOrarioSquadra())*2;
			ore = minutiLavoroStraordinario / 60;
			costoOrarioStraordinario *= ore;
			
			
		}
		//Calcolo il costo totale monetario della squadra
		costoTotale = costoOrarioNormale + costoOrarioStraordinario + costoConsumo;
		
		return costoTotale;
		
	}

	/**
	 * Metodo per la determinazione del tempo CONSIDERANDO l'ordine di esecuzione di una lista di interventi 
	 * affidati a una squadra
	 * 
	 * @param listaInterventi array di interi rappresentati gli id degli interventi
	 * @param idSquadra String rappresentante l'id della squadra
	 * @return tempo di esecuzione
	 */
	
	public static float tempoInterventiPerSquadra(int[] listaInterventi,String idSquadra){
		float durataSpostamenti=0;
		float durataInterventi=0;
		float durataGiornataTotale=0;
		
		String idImpiantoAttuale = idSedediPartenza;
		String idImpiantoSuccessivo;
		
		//Calcolo la distanza totale per visitare tutti gli impianti della lista di interventi
		for(int i=0;i<listaInterventi.length;i++){
			//CONTROLLARE QUESTA ISTRUZIONE
			int indice = getIndexFromId_Intervention(listaInterventi[i]); //Controllare
					
			idImpiantoSuccessivo = interventi.get(indice).getIdImpianto();
					
			if(! (idImpiantoAttuale.equals(idImpiantoSuccessivo)) ){
				//AGGIORNO IL TEMPO TOTALE DEGLI SPOSTAMENTI
				durataSpostamenti+=getDurataSpostamentiImpinatiHashNew(idImpiantoAttuale, idImpiantoSuccessivo);
						
				//AGGIORNO IL TEMPO TOTALE DI INTERVENTI
				durataInterventi+=interventi.get(indice).getDurata();
					
				//IMPOSTO AGGIORNO L'IMPIANTO ATTUALE
				idImpiantoAttuale = idImpiantoSuccessivo;
			}
			else{
				durataInterventi+=interventi.get(indice).getDurata();
			}
					
		}
		//AGGIUNGO ALLA DISTANZA PERCORSA E AL TEMPO DI SPOSTAMENTO IL RITORNO ALLA SEDE DI PARTENZA
		//SE NON SONO GIA' NELLA SEDE DI PARTENZA
		if(! (idImpiantoAttuale.equals(idSedediPartenza)) ){
			
			durataSpostamenti+=getDurataSpostamentiImpinatiHashNew(idImpiantoAttuale, idSedediPartenza);
			
		}
				
		//RICAVO  I MINUTI PER ESEGUIRE INTERVENTI E SPOSTAMENTI
		
		durataGiornataTotale = durataSpostamenti + durataInterventi;
				
		return durataGiornataTotale;
	}

	/**
	 * Metodo per la determinazione della distanza CONSIDERANDO l'ordine di esecuzione di una lista di interventi 
	 * affidati a una squadra
	 * 
	 * @param listaInterventi array di interi rappresentati gli id degli interventi
	 * @param idSquadra String rappresentante l'id della squadra
	 * @return distanza di esecuzione
	 */
	public static float distanzaInterventiPerSquadra(int[] listaInterventi,String idSquadra){
		
		float distanzaTotale=0;
		
		String idImpiantoAttuale = idSedediPartenza;
		String idImpiantoSuccessivo;
		
		
		//Calcolo la distanza totale per visitare tutti gli impianti della lista di interventi
		for(int i=0;i<listaInterventi.length;i++){
			//CONTROLLARE QUESTA ISTRUZIONE
			int indice = getIndexFromId_Intervention(listaInterventi[i]); //Controllare
					
			idImpiantoSuccessivo = interventi.get(indice).getIdImpianto();
					
			if(! (idImpiantoAttuale.equals(idImpiantoSuccessivo)) ){
				//AGGIORNO LA DISTANZA TOTALE PERCORSA
						
				distanzaTotale+=getDistanzaSpostamentiImpinatiHashNew(idImpiantoAttuale, idImpiantoSuccessivo);
						
					
				//IMPOSTO AGGIORNO L'IMPIANTO ATTUALE
				idImpiantoAttuale = idImpiantoSuccessivo;
			}	
		}
		//AGGIUNGO ALLA DISTANZA PERCORSA IL RITORNO ALLA SEDE DI PARTENZA
		//SE NON SONO GIA' NELLA SEDE DI PARTENZA
		if(! (idImpiantoAttuale.equals(idSedediPartenza)) ){
			distanzaTotale+=getDistanzaSpostamentiImpinatiHashNew(idImpiantoAttuale, idSedediPartenza);
		}
		
		return distanzaTotale;
		
	}

	
	/**
	 * Metodo di supporto per verificare se un dato intervento può essere eseguito da una specifica squadra
	 * @param indiceIntervento 
	 * @param indiceSquadra
	 * @return true se l'intervetnto puo' essere eseguito dalla squadro, false altrimenti
	 */
	public static boolean checkIntSquad(int idIntervento, int indexSquad){
		
		int indexInt = getIndexFromId_Intervention(idIntervento);
		
		
		ArrayList<String> compIntervneto = interventi.get(indexInt).getCompetenze();
		ArrayList<String> compSquadra = squadre.get(indexSquad).getCompentenze();
		
		for(int i=0; i < compIntervneto.size(); i++){
			if(!compSquadra.contains(compIntervneto.get(i))){
				return false;
			}	
		}
		return true;
	}
	
	/**
	 * Metodi di supporto che ritorno l'indice dell'ArrayList di Interventi il cui elemento 
	 * ha id uguale a @param idIntervention
	 * @return
	 */
	public static int getIndexFromId_Intervention(int idIntervention){
		int result = -1;
		for(int i=0; i<interventi.size(); i++){
			if(Integer.valueOf(interventi.get(i).getId()) == idIntervention){
				result = i;
				return result;
			}
		}
		return result;
	}
	
	/**
	 * Metodi di supporto che ritorno l'indice dell'ArrayList  @param listaInt 
	 * di Interventi il cui elemento ha id uguale a @param idIntervention
	 * @return idex
	 */
	public static int getIndexFromId_Intervention(int idIntervention, ArrayList<Intervento> listaInt){
		int result = -1;
		for(int i=0; i<listaInt.size(); i++){
			if(Integer.valueOf(listaInt.get(i).getId()) == idIntervention){
				result = i;
				return result;
			}
		}
		return result;
	}

	/**
	 * Metodo di supporto per ottenere una lista di squadre che hanno le competenze
	 * per eseguire un dato intervento
	 * @param idIntervento
	 * @return
	 */
	public static ArrayList<String> getListaSquadForInt(int idIntervento){
		ArrayList<String> result = new ArrayList<String>();
		for(int i=0;i<squadre.size();i++){
			if(checkIntSquad(idIntervento, i)){
				result.add(new String(String.valueOf(i)));
			}
		}
		return result;
	}
	
	/**
	 * Metodo di supporto per ottenere una lista di squadre che hanno le competenze per
	 * eseguire l'intervento con id @param idIntervento. La lista ritornata non avrà tra i suoi
	 * elemente l'indice della squadra @param indexTeam
	 * @return
	 */
	public static ArrayList<String> getListaSquadForInt(int idIntervento, int indexTeam){
		ArrayList<String> result = new ArrayList<String>();
		for(int i=0;i<squadre.size();i++){
			if(i!=indexTeam){
				if(checkIntSquad(idIntervento, i)){
					result.add(new String(String.valueOf(i)));
				}
			}
		}
		return result;
	}
	
	/**
	 * Metdo di supporto per effettuare la mossa per la modifica della soluzione iniziale
	 * per la realizzazione di una soluzione vicina effettuando lo swap di un intervento
	 * all'interno della lista di interventi di una squadra
	 * Il metodo al suo termine modificherà la soluzione da valutare
	 */
	public static void mossaStessaSquadra(){
		int selectedSquad = sRandom.nextInt(squadre.size());
		//Controllo che la squadra selazionata abbia ameno due interventi nella sua lista
		while(valuateSolution.getListaIntIntervento(selectedSquad).size() < 2){
			selectedSquad = sRandom.nextInt(squadre.size());
		}
		
		
		int elementToSwap1 = sRandom.nextInt(
				valuateSolution.getListaIntIntervento(selectedSquad).size());
				
		
		int elementToSwap2 = sRandom.nextInt(
				valuateSolution.getListaIntIntervento(selectedSquad).size());
		
		//Controllo che gli interventi selezionati per lo swap non siano coincidenti
		while(elementToSwap1 == elementToSwap2){
			
			elementToSwap2 = sRandom.nextInt(
					valuateSolution.getListaIntIntervento(selectedSquad).size());
		}
		
		valuateSolution.swapIntSameSquad(selectedSquad,elementToSwap1,elementToSwap2);
	}
	
	
	
	//DA TESTARE
	/**
	 * Metdo di supporto per effettuare la mossa per la modifica della soluzione iniziale
	 * per la realizzazione di una soluzione vicina effettuando lo swap di un intervento
	 * all'interno della lista di interventi di una squadra considerando la priorità
	 * degli interventi
	 * Il metodo al suo termine modificherà la soluzione da valutare
	 */
	public static void mossaStessaSquadraPriority(){
		int count = 0;
		ArrayList<ArrayList<Intervento>> twoListP = new ArrayList<ArrayList<Intervento>>();
		int selectedSquad;
		do{
			//System.out.print("1");
			selectedSquad = sRandom.nextInt(squadre.size());
			twoListP = getListPriority(selectedSquad);
			count++;
		}
		
		//Controllo che la squadra selazionata abbia almeno due interventi nella sua lista
		//Inoltre controllo che la squadra selezionata abbia almeno due elementi di medesima
		//piorità
		//while(valuateSolution.getListaIntIntervento(selectedSquad).size() < 2 && 
		while(twoListP.get(0).size() < 2 && twoListP.get(1).size() < 2 && count!=20); //{
		
		if(count==20){
			//DEBUG
			//System.out.println("SWAP SAME TEAM IS IMPOSSIBLE !!!");
			//System.out.println("Squadra diversa");
			mossaSquadraDiversa();
			return;
		}
		//	selectedSquad = sRandom.nextInt(squadre.size());
		//	twoListP = getListPriority(selectedSquad);
			
		//}
		
		//Estraggo casualmente un intervento della squadra scelta
		int elementToSwap1;
		do{
			//System.out.print("2");
			elementToSwap1 = sRandom.nextInt(valuateSolution.getListaIntIntervento(selectedSquad).size());
		}
		//Mi accerto che l'intervento scelto per lo swap abbia almeno un altro di pari priorità
		while(twoListP.get(valuateSolution.getListaIntIntervento(selectedSquad).get(elementToSwap1).getPriority()).size() <2);//{
			//elementToSwap1 = sRandom.nextInt(valuateSolution.getListaIntIntervento(selectedSquad).size());
		//}
		
		//Estraggo un secondo elemento diverso dal precedente
		
		int elementToSwap2;
		do{
			//System.out.print("3");
			elementToSwap2= sRandom.nextInt(valuateSolution.getListaIntIntervento(selectedSquad).size());
		}while(valuateSolution.getListaIntIntervento(selectedSquad).get(elementToSwap2).getPriority() !=
				valuateSolution.getListaIntIntervento(selectedSquad).get(elementToSwap2).getPriority()
				|| elementToSwap1 == elementToSwap2);
		
			
		//ricavo l'indice del secondo elemento in riferimento alla lista di interventi valuateSolution
		/*int indxSwap2;
		if(valuateSolution.getListaIntIntervento(selectedSquad).get(elementToSwap1).getPriority() == 1)
			indxSwap2 = elementToSwap2;
		else
			indxSwap2 = twoListP.get(1).size() + elementToSwap2;
		//Controllo che gli interventi selezionati per lo swap non siano coincidenti
		while(elementToSwap1 == indxSwap2){
			
			elementToSwap2 = sRandom.nextInt(twoListP.get(
					valuateSolution.getListaIntIntervento(selectedSquad).get(elementToSwap1).getPriority()
					).size());
			
			if(valuateSolution.getListaIntIntervento(selectedSquad).get(elementToSwap1).getPriority() == 1)
				indxSwap2 = elementToSwap2;
			else
				indxSwap2 = twoListP.get(1).size() + elementToSwap2;
		}
		*/
		/*int idInt1= Integer.valueOf(
				valuateSolution.getListaIntIntervento(selectedSquad).get(elementToSwap1).getId()
				);
		int idInt2= Integer.valueOf(
				valuateSolution.getListaIntIntervento(selectedSquad).get(elementToSwap2).getId()
				);
		*/
		//System.out.println("SWAP!!!");
		valuateSolution.swapIntSameSquad(selectedSquad,elementToSwap1,elementToSwap2);
	}
	
	/**
	 * Metodo che data l'id di una squadra ritorna un doppio ArrayList
	 * Il primo ArrayList contiene tutti gli interventi con priorità 1
	 * Ilsecondo Array List contiene tutt gli interventi con priorità 0
	 * 
	 * @param idTeam
	 * @return ArrayList doppio con liste degli interventi suddivise per priorità
	 */	
	public static ArrayList<ArrayList<Intervento>> getListPriority(int idTeam){
		ArrayList<ArrayList<Intervento>> result = new ArrayList<ArrayList<Intervento>>();
		ArrayList<Intervento> listIntPri1 = new ArrayList<Intervento>(); 
		ArrayList<Intervento> listIntPri0 = new ArrayList<Intervento>();
		for(int i = 0; i< valuateSolution.getListaIntIntervento(idTeam).size(); i++){
			if( valuateSolution.getListaIntIntervento(idTeam).get(i).getPriority() == 1)		
				listIntPri1.add(valuateSolution.getListaIntIntervento(idTeam).get(i));
			else
				listIntPri0.add(valuateSolution.getListaIntIntervento(idTeam).get(i));
		}
		result.add(listIntPri0);
		result.add(listIntPri1);
		return result;
	}
	
	/**
	 * MetOdo di supporto per effettuare la mossa per la modifica della soluzione iniziale
	 * per la realizzazione di una soluzione vicina effettuando lo swap di un intervento 
	 * dalla lista di interventi di una squadra all'interno della lista di interventi di 
	 * un' alatra squadra
	 */
	public static void mossaSquadraDiversa(){
		boolean reselectTeam = false;
		int selectedSquad1;
		ArrayList<String> listaPossSquadre = new ArrayList<String>();
		
		// Seleziono un team che abbia assegnato nella sua lista almeno un
		// intervento
		do {
			do {
				selectedSquad1 = sRandom.nextInt(squadre.size());
			} while (valuateSolution.getListaIntIntervento(selectedSquad1).size() < 1);

			//Caso in cui la squadra selezionata ha solo un elemento 
			if (valuateSolution.getListaIntIntervento(selectedSquad1).size() == 1) {
				
				// Ricavo l'id dell'unico intervento posseduto dalla squadra
				int idIntToDonate = Integer.valueOf(valuateSolution.getListaIntIntervento(selectedSquad1).get(0).getId());

				//Ricavo la lista delle squadre in grado di eseguire l'intervento oltre alla squadra selezionata
				listaPossSquadre = getListaSquadForInt(idIntToDonate, selectedSquad1);
				//Se la lista ricavata è vuota setto il parametro reselectTeam per ripetere il ciclo con una nuova squadra
				if (listaPossSquadre.size() == 0)
					reselectTeam = true;
				// Se la lista delle squadre in grado di eseguire l'intervento non è vuota scelgo una squadra casualmente
				// ed eseguo lo swap di squadra
				else{
					int selectedSquad2 = Integer.valueOf(listaPossSquadre.get(sRandom.nextInt(listaPossSquadre.size())));
					valuateSolution.swapIntFromTwoSquadPriority(selectedSquad1,selectedSquad2,idIntToDonate);
					reselectTeam = false;
				}
			}
			// Caso in cui la squadra ha più di un intevrento
			else{
				// Selezione in modo random un intervento 
				int sizeList = valuateSolution.getListaIntIntervento(selectedSquad1).size();
				int idIntToDonate = Integer.valueOf(valuateSolution.getListaIntIntervento(selectedSquad1).get(
						sRandom.nextInt(sizeList)).getId());
								
				//Ricavo la lista delle squadre in grado di eseguire l'intervento oltre alla squadra selezionata
				listaPossSquadre = getListaSquadForInt(idIntToDonate, selectedSquad1);
				//Se la lista ricavata è vuota setto il parametro reselectTeam per ripetere il ciclo con una nuova squadra
				if (listaPossSquadre.size() == 0)
					reselectTeam = true;
				// Se la lista delle squadre in grado di eseguire l'intervento non è vuota scelgo una squadra casualmente
				// ed eseguo lo swap di squadra
				else{
					int selectedSquad2 = Integer.valueOf(listaPossSquadre.get(sRandom.nextInt(listaPossSquadre.size())));
					valuateSolution.swapIntFromTwoSquadPriority(selectedSquad1,selectedSquad2,idIntToDonate);
					reselectTeam = false;
				}
			}
		} while (reselectTeam);
	}
	
	/**
	 * Metodo per la generazione pseudocasuale della soluzione iniziale
	 * Nel dettaglio vengono distribuiti gli interventi alla squadre
	 * 
	 */
	public static void createInitialSolution(){
		
		initialSolution.clear();
		
		File file = new File("initSolution.txt");
		//Ricarico la soluzione da file se il file esiste
		//DISABILITO MOMENTANEAMENTE QUESTA FUNZIONALITA'
		/*
		if(file.exists()){
			restoreInitSolutionFromFile("initSolution.txt");
		}*/
		
		//ELIMINO il file della soluzione se esiste in modod da non dipendere
		//dalla prima soluzione creata
		
		if (file.exists()){
			file.delete();
		}
		//else{
			interventiNonAssegnati = new ArrayList<Intervento>(interventi);
			for(int i=0; i<interventi.size(); i++){
				int indiceSquadra = sRandom.nextInt(squadre.size());
				
				Intervento intASS = interventi.get(i); //intervento da assegnare
				
				// Verifico che la squadra selezionata abbia le skills
				// in caso negativo ne seleziono una nuova in modo random 
				while(!(checkIntSquad(Integer.valueOf(intASS.getId()), indiceSquadra)) ){
					indiceSquadra = sRandom.nextInt(squadre.size());
				}
				
				//aggiungo 1 all'indice i perchè il metodo addIntervento lavora
				//sugli id degli interventi e non sugli indici di memorizzazione
				int idInt=Integer.valueOf(intASS.getId());
				//initialSolution.addIntervento(idInt, indiceSquadra);
				//Utilizzo la funzione di inserimento che tiene conto del 
				//vincolo sulla priorità
				initialSolution.addInterventoPriority(idInt, indiceSquadra);
				interventiNonAssegnati.remove(intASS);//riumuovo l'intervneto dalla lista degli nitnerventi non asegnati
			
			}
			valuateSolution = new SolutionInOrder(initialSolution);
			
			openFile("initSolution.txt");
			printToFile(initialSolution.toString()+"\n");
			closefile();
		//}
	}
	
	/**
	 * Metodo per la generazione di una soluzione iniziale uniforme
	 */
	public static void createInitialSolutionUniform() {
		initialSolution.clear();

		File file = new File("initSolution.txt");
		if (file.exists()) {
			restoreInitSolutionFromFile("initSolution.txt");
		}

		else {

			interventiNonAssegnati = new ArrayList<Intervento>(interventi);

			int dist = interventi.size() / squadre.size(); // numero di
															// interveti minimo
															// di ogni squadra
			boolean flag = true; // flag per segnalare che tutte le squadre
									// hanno un numero minimo di interventi

			for (int i = 0; i < interventi.size(); i++) {
				int indiceSquadra = sRandom.nextInt(squadre.size());
				Intervento intASS = interventi.get(i); // intervento da
														// assegnare
				// Ciclo per assegnare almeno un numero pari a m/n interve a
				// tutte le squadre
				if (flag) {
					// Seleziono una nuova squadra casualmente se la squadra
					// scelta non ha le competenze oppure
					// ha gia il numero massimo uniforme
					int size = initialSolution.getListaIntIntervento(
							indiceSquadra).size();
					while ((!checkIntSquad(i, indiceSquadra)) || (size >= dist)) {
						indiceSquadra = sRandom.nextInt(squadre.size());
						size = initialSolution.getListaIntIntervento(
								indiceSquadra).size();
					}
					int idInt = i + 1;
					initialSolution.addIntervento(idInt, indiceSquadra);
					interventiNonAssegnati.remove(intASS);// riumuovo
															// l'intervneto
															// dalla lista degli
															// nitnerventi non
															// asegnati
					// Verifico che sia rimasta almeno una squadra che abbia
					// meno interventi di m/n
					flag = false;
					for (int j = 0; j < squadre.size(); j++) {
						if (initialSolution.getListaIntIntervento(j).size() < dist) {
							flag = true;
						}
					}
				} else {// (!flag){
						// Seleziono una nuova squadra casualmente se la squadra
						// scelta non ha le competenze oppure
						// ha gia il numero massimo uniforme
					while (!checkIntSquad(i, indiceSquadra)) {
						indiceSquadra = sRandom.nextInt(squadre.size());
					}
					int idInt = i + 1;
					initialSolution.addIntervento(idInt, indiceSquadra);
					interventiNonAssegnati.remove(intASS);// riumuovo
															// l'intervneto
															// dalla lista degli
															// nitnerventi non
															// asegnati
					// Verifico che sia rimasta almeno una squadra che abbia
					// meno interventi di m/n
				}

			}
			openFile("initSolution.txt");
			printToFile(initialSolution.toString() + "\n");
			closefile();
		}
		valuateSolution = new SolutionInOrder(initialSolution);

	}

	/**
	 * Metodo utilizzato per ripristinare la soluzione iniziale generata in modo randomico
	 *  e salvata su di un file di supporto
	 * @param fileName
	 */
	public static void restoreInitSolutionFromFile(String fileName){
		
		initialSolution.clear();
		
		File file = new File (fileName);
		if(file.exists()){
			try {
				FileReader myFileReader = new FileReader(file);
				BufferedReader myBufferReader = new BufferedReader(myFileReader);
				String line = myBufferReader.readLine();
				String linePenultima = new String();;
				while(line != null){
					linePenultima=line;
					line = myBufferReader.readLine();
				}
				//Suddivido la line in token in modo da estrapolare gli interventi per ogni squadra
				ArrayList<String> listaInterventiFromFile = new ArrayList<String>();
				float costoTot = 0;
				StringTokenizer myTokenizer = new StringTokenizer(linePenultima,":");
				String comodo = myTokenizer.nextToken();
				String comodo2 = myTokenizer.nextToken();
				do {

					if (comodo.matches("^s.*") && !comodo2.matches("^C.*")
							&& !comodo2.matches("^s.*")) {
						listaInterventiFromFile.add(comodo2);
						comodo = comodo2;
						comodo2 = myTokenizer.nextToken();
					} else {
						if (comodo.matches("^s.*") && comodo2.matches("^s.*")) {
							listaInterventiFromFile.add(" ");
							comodo = comodo2;
							comodo2 = myTokenizer.nextToken();
						} else {
							if (comodo.matches("^s.*")
									&& comodo2.matches("^C.*")) {
								listaInterventiFromFile.add(" ");
								comodo = comodo2;
								comodo2 = myTokenizer.nextToken();
							} else {
								comodo = comodo2;
								comodo2 = myTokenizer.nextToken();
							}
						}
					}

				} while (myTokenizer.hasMoreTokens());
				
				//solutionInOrder minSolution = new solutionInOrder();
				initialSolution.setCostoTotale(costoTot);

				initialSolution.setListaInterventi(listaInterventiFromFile);
				restoreToInitialSolution();

				myBufferReader.close();
				myFileReader.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * Metodo per la generazione di una soluzione vicina alla soluzione di partenza.
	 * La generazione della soluzione vicina viene effettuata tenendo conto di due 
	 * parametri nStessaSquadra e nSquadraDiversa, tramite questi si decide quanto
	 * la generazione della soluzione vicina si distacca dalla soluzione di partenza
	 * 
	 * @param nStessaSquadra numero di scambi da effettuare all'interno della stassa squadra
	 * @param nSquadraDiversa numer di scambi da effettuare su squadre differenti
	 */
	public static void createNearSolution(int nStessaSquadra, int nSquadraDiversa){
		for(int i=0;i<nStessaSquadra;i++){
			//DEBUG
			//System.out.println("Stessa Squadra");
			mossaStessaSquadraPriority();
		}
		for(int i=0; i<nSquadraDiversa;i++){
			//DEBUG
			//System.out.println("Squadra Diversa");
			mossaSquadraDiversa();
		}
	}

	
	/**
	 * Metodo per la memorizzazione della soluzione da valutare nella soluzione
	 * reputata come accettata cioè quella iniziale
	 */
	public static void acceptNewSolution(){
		initialSolution.setListaInterventi2(valuateSolution.getListaInterventi());
	}
	
	/**
	 * Metodo per il ripristino della soluzione da valutare con quella iniziale
	 * (cioe' quella considetara come l'ultima accettata)
	 */
	
	public static void restoreToInitialSolution(){
		valuateSolution.setListaInterventi2(initialSolution.getListaInterventi());
	}
	
	/**
	 * Metodo per la memorizzazione del valore della soluzione trovata all'interno di 
	 * una Mappa<Valore,Numero> dove vengono memorizzate il numero di occorrenze di un
	 * valore trovato 
	 */
	public static void saveInitSolution(float value){
		risultati.add(value);
		/*if(risultati.containsKey(initialSolution.getCosto())){
			int occorenze = risultati.get(initialSolution.getCosto());
			occorenze++;
			risultati.put(initialSolution.getCosto(), occorenze);
		}
		else
			risultati.put(initialSolution.getCosto(), 1);*/
	}
	
	/**
	 * Metodo di supporto per la chiusura di un file
	 */
	public static void closefile() {
		try {
			myBufferWriter.flush();
			myBufferWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Metodo di supporto per la sctrittura di un file
	 * @param value
	 */
	public static void printToFile(String value) {
		try {
			myBufferWriter.write(value);
			myBufferWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	/**
	 * Metodo di supporto l'apertura di un file
	 */
	public static void openFile(String value) {
		// TODO Auto-generated method stub
		try {
			//SimpleDateFormat dataFormat = new SimpleDateFormat("dd-MM-yyy-HH-mm-ss");
			//Date dateNow = new Date();
			myWriter= new FileWriter(value,true);//+dataFormat.format(dateNow)+".txt",true);
			myBufferWriter = new BufferedWriter(myWriter);
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	/**
	 * 
	 * @param value
	 */
	public static void distribuzionePercentuale(String value){
		
		double ottimo = 396.9586181640625;//ottimo della soluzione
		
		TreeMap<Integer,Double> percentuali = new TreeMap<Integer,Double>();
		/*for(int i=1;i<=10;i++)
			percentuali.put(i,0);
		*/
		double unpercentoOttimo = (ottimo * 1)/100;
		for(double d : risultati){
			int key = (int) Math.ceil((d-ottimo)/unpercentoOttimo);
			
			if(percentuali.containsKey(key)){
				double occorrenze = percentuali.get(key);
				//occorrenze++;
				occorrenze+=0.5;
				percentuali.put(key, occorrenze);
			}
			else{
				percentuali.put(key, 0.5);
			}
		}
		
		openFile("result_test_percent_"+value+".txt");
		int index = 0;
		for(int key : percentuali.navigableKeySet()){
			printToFile(index +"\t"+key+"%\t"+percentuali.get(key)+"%\n");
			index++;
		}
		closefile();
	}
	
	/**
	 * 
	 * @param nIteration
	 * @param nValuate
	 * @param cooling
	 * @param alfa
	 */
	public static void createMediaIterTime(int nIteration, int nValuate, double cooling, double alfa){
		String fileName = "iteration_time"+nIteration+"_"+nValuate+"_"+cooling+"_"+alfa+".it";
		int number=0;
		double iterationMed=0;
		double timeMed=0;
		File file = new File (fileName);
		try {
			FileReader myFileReader = new FileReader(file);
			BufferedReader myBufferReader = new BufferedReader(myFileReader);
			String line = myBufferReader.readLine();
			do{
				number++;
				StringTokenizer myTokenizer = new StringTokenizer(line,"\t");
				//while(myTokenizer.hasMoreTokens()){
					String comodo = myTokenizer.nextToken();
					iterationMed+=Integer.valueOf(comodo);
					
					comodo = myTokenizer.nextToken();
					timeMed+=Integer.valueOf(comodo);
				//}
			line = myBufferReader.readLine();
			}
			while(line!=null);
			myBufferReader.close();
			myFileReader.close();
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		iterationMed = iterationMed/number;
		timeMed = timeMed/number;
		DataStructure.Utility.openFile("media_"+fileName);
		DataStructure.Utility.printToFile(iterationMed+"\t"+timeMed);
		DataStructure.Utility.closefile();
	}
}
