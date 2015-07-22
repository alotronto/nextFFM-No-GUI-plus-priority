package StructureClass;
import java.util.ArrayList;


public class Intervento {

	private String id;
	private ArrayList<String> competenze;
	private String idImpianto;
	private float durata;
	
	private int priority;

	public Intervento(String id) {
		this.id = id;
		competenze = new ArrayList<String>();
	}

	/**
	 * Metodo get per Id dell' intervento
	 * 
	 * @return id intervento
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Metodo set per l'id dell'impianto in cui si trova l'intervento
	 * @param idImpianto
	 */
	public void setIdImpianto(String idImpianto){
		this.idImpianto=idImpianto;
	}
	
	/**
	 * Metodo get per l'idImpianto dell'intervneto 
	 * @return ritorna uno String con l'id dell'impianto
	 */
	public String getIdImpianto(){
		return this.idImpianto;
	}
	
	/**
	 * Metodo set per la durata dell'intervento
	 * @param durata
	 */
	public void setDurata(float durata){
		this.durata = durata;
	}
	
	/**
	 * Metodo get per la durata dell'intervento
	 * @return int che rappresenta la dutata dell'intervento
	 */
	public float getDurata(){
		return this.durata;
	}
	
	/**
	 * Metodo per il setting delle competenze di un intervento
	 * 
	 * @param compentenza String competenza
	 */
	public void setCompentenza(String compentenza){
		competenze.add(new String(compentenza));
		
	}
	
	/**
	 * Metdo per il get delle competenze di un intervento
	 * @return ArrayList<String> delle competenze
	 */
	public ArrayList<String> getCompetenze(){
		return competenze;
	}
	
	/**
	 * Metodo per il get della priorità di un intervento
	 * @return int priorità dell'intervento 0 normale 1 alta
	 */
	public int getPriority(){
		return this.priority;
	}
	/**
	 * Metodo per il set della priorità di un intervento
	 * @param priority int priorità dell'intervento 0 normale 1 alta
	 */
	public void setPriority(int priority){
		this.priority = priority;
	}
}
