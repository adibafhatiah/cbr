package models;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import play.db.jpa.Model;

@Entity
@Table(name="derajatgejala", uniqueConstraints={@UniqueConstraint(columnNames={"kode_derajatgejala"})})
public class DerajatGejala extends Model {
	public String kode_derajatgejala;
	public int nilai;
	public String keterangan;
	
	public String toString(){
		return Integer.toString(nilai);		
	}
}
