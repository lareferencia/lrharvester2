package org.lareferencia.backend.stats;

import java.util.HashMap;
import java.util.Map;

import org.lareferencia.backend.harvester.OAIRecordMetadata;
import org.lareferencia.backend.util.datatable.ColumnDescription;
import org.lareferencia.backend.util.datatable.DataTable;
import org.lareferencia.backend.util.datatable.TypeMismatchException;
import org.lareferencia.backend.util.datatable.ValueType;
import org.lareferencia.backend.validator.ValidationResult;


public class ByRepositoryStatProcessor extends BaseStatProcessor {
	
	private static String SEPARATOR = "#@#";
	
	String repositoryNameField = "dc:source";

	String repositoryNamePrefix = "reponame:";
	String institutionNamePrefix = "instname:";
	
	protected Map<String, Integer[]> countMap = new HashMap<String, Integer[]>();	

	
	protected enum DataIndex { ALL, VALID, TRANSFORMED }
	
	
	protected void updateCounter(String key, DataIndex index) {
		
		Integer[] data = countMap.get(key);
		
		if ( data == null ) {
			Integer[] zero_data = {0,0,0};
			countMap.put(key,zero_data);
			data = zero_data;
		}
		
		data[index.ordinal()] = data[index.ordinal()] + 1;
	}

	@Override
	public void addObservation(OAIRecordMetadata metadata, ValidationResult validationResult, Boolean wasTransformed) {

		String institution = "";
		String repository = "ALL";
		
		for (String occr: metadata.getFieldOcurrences(repositoryNameField) ) {
			
			if ( occr.startsWith(institutionNamePrefix) ) 
				institution = occr.substring( institutionNamePrefix.length() );
			
			
			if ( occr.startsWith(repositoryNamePrefix) ) 
				repository = occr.substring( repositoryNamePrefix.length() );
		}
		
		String key = institution.trim() + SEPARATOR + repository.trim();
		
		updateCounter(key, DataIndex.ALL);
		
		if (validationResult.isValid())
			updateCounter(key, DataIndex.VALID);
		
		if (wasTransformed)
			updateCounter(key, DataIndex.TRANSFORMED);
			
			
	}

	@Override
	public DataTable getStats() {
		
		DataTable resultTable = new DataTable();
		
		// Construye las columnas de la tablas de resultados
		resultTable.addColumn( new ColumnDescription("C1", ValueType.TEXT , "Institution") );
		resultTable.addColumn( new ColumnDescription("C2", ValueType.TEXT , "Repository") );
		resultTable.addColumn( new ColumnDescription("C3", ValueType.NUMBER , "Total") );
		resultTable.addColumn( new ColumnDescription("C4", ValueType.NUMBER , "Valid") );
		resultTable.addColumn( new ColumnDescription("C5", ValueType.NUMBER , "Trasformed") );

		// Para cada campo
		for ( String key: countMap.keySet() ) {			
			
			// Obtiene el registros de conteos para ese campo
			Integer[] counts = countMap.get(key);
			
			// Calcula los porcentajes relativos
			Integer total = counts[DataIndex.ALL.ordinal()];
			Integer valid = counts[DataIndex.VALID.ordinal()];
			Integer transformed = counts[DataIndex.TRANSFORMED.ordinal()];

			String inst = key.split(SEPARATOR)[0];
			String repo = key.split(SEPARATOR)[1];
			
			// Agrega una fila con los resultados de los conteos
			try {
				resultTable.addRowFromValues(inst, repo, total, valid, transformed);
				
			} catch (TypeMismatchException e) {
				e.printStackTrace();
				System.err.println("Error en los tipos de columnas al contruir DataTable StatProcessor");
			}			
		}
		
		return resultTable;
	}
}
