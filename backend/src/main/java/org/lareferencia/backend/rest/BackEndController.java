/*******************************************************************************
 * Copyright (c) 2013 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Lautaro Matas (lmatas@gmail.com) - Desarrollo e implementación
 *     Emiliano Marmonti(emarmonti@gmail.com) - Coordinación del componente III
 * 
 * Este software fue desarrollado en el marco de la consultoría "Desarrollo e implementación de las soluciones - Prueba piloto del Componente III -Desarrollador para las herramientas de back-end" del proyecto “Estrategia Regional y Marco de Interoperabilidad y Gestión para una Red Federada Latinoamericana de Repositorios Institucionales de Documentación Científica” financiado por Banco Interamericano de Desarrollo (BID) y ejecutado por la Cooperación Latino Americana de Redes Avanzadas, CLARA.
 ******************************************************************************/
package org.lareferencia.backend.rest;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.lareferencia.backend.domain.Network;
import org.lareferencia.backend.domain.NetworkSnapshot;
import org.lareferencia.backend.domain.OAIOrigin;
import org.lareferencia.backend.domain.OAIRecord;
import org.lareferencia.backend.domain.RecordStatus;
import org.lareferencia.backend.domain.RecordValidationResult;
import org.lareferencia.backend.domain.SnapshotStatus;
import org.lareferencia.backend.domain.Validator;
import org.lareferencia.backend.domain.ValidatorRule;
import org.lareferencia.backend.harvester.OAIRecordMetadata;
import org.lareferencia.backend.indexer.IIndexer;
import org.lareferencia.backend.indexer.IndexerManager;
import org.lareferencia.backend.indexer.IndexerWorker;
import org.lareferencia.backend.repositories.NetworkRepository;
import org.lareferencia.backend.repositories.NetworkSnapshotLogRepository;
import org.lareferencia.backend.repositories.NetworkSnapshotRepository;
import org.lareferencia.backend.repositories.OAIOriginRepository;
import org.lareferencia.backend.repositories.OAIRecordRepository;
import org.lareferencia.backend.repositories.OAISetRepository;
import org.lareferencia.backend.repositories.RecordValidationResultRepository;
import org.lareferencia.backend.rest.BackEndController.NetworkInfo;
import org.lareferencia.backend.tasks.SnapshotManager;
import org.lareferencia.backend.util.JsonDateSerializer;
import org.lareferencia.backend.validation.ValidationManager;
import org.lareferencia.backend.validation.transformer.ITransformer;
import org.lareferencia.backend.validation.validator.IValidator;
import org.lareferencia.backend.validation.validator.ValidatorResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.FacetOptions;
import org.springframework.data.solr.core.query.FacetOptions.FacetSort;
import org.springframework.data.solr.core.query.FacetQuery;
import org.springframework.data.solr.core.query.Field;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleFacetQuery;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.SimpleStringCriteria;
import org.springframework.data.solr.core.query.result.FacetFieldEntry;
import org.springframework.data.solr.core.query.result.FacetPage;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Handles requests for the application home page.
 */
@Controller
public class BackEndController {

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private NetworkRepository networkRepository;

	@Autowired
	private OAIOriginRepository originRepository;

	@Autowired
	private OAISetRepository setRepository;

	@Autowired
	private NetworkSnapshotRepository networkSnapshotRepository;

	@Autowired
	private NetworkSnapshotLogRepository networkSnapshotLogRepository;

	@Autowired
	private OAIRecordRepository recordRepository;
	
	@Autowired
	private RecordValidationResultRepository validationResultRepository;
	
	@Autowired
	private SolrTemplate solrTemplate;
	

	@Autowired
	private ValidationManager validationManager;

	@Autowired
	private IndexerManager indexerManager;

	@Autowired
	TaskScheduler scheduler;

	@Autowired
	SnapshotManager snapshotManager;

	/******************************************************
	 * Login Services
	 ******************************************************/

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Locale locale, Model model) {
		return "home";
	}
	
	@RequestMapping(value = "/home", method = RequestMethod.GET)
	public String home2(Locale locale, Model model) {
		return "home";
	}

	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String login(Locale locale, Model model) {
		return "login";
	}

	@RequestMapping(value = "/login", params = "errorLogin", method = RequestMethod.GET)
	public String loginFailed(Locale locale, Model model) {
		model.addAttribute("loginFailed", true);
		return "login";
	}


	/******************************************************
	 * Diagnose Services
	 ******************************************************/


	@ResponseBody
	@RequestMapping(value = "/public/getRecordMetadataByID/{id}", method = RequestMethod.GET)
	public String getRecordMetadataByID(@PathVariable Long id) throws Exception {

		OAIRecord record = recordRepository.findOne(id);

		if (record != null)
			return record.getPublishedXML();
		else
			return "Registro inexistente - Posiblemente el diagnóstico está desactualizado";

	}
	
	
	
	@RequestMapping(value = "/public/diagnose/{snapshotID}/[{fq}]", method = RequestMethod.GET)
	@ResponseBody
	public DiagnoseResult diagnoseListRules(@PathVariable Long snapshotID, @PathVariable List<String> fq) throws Exception {
		
	
		NetworkSnapshot snapshot = networkSnapshotRepository.findOne(snapshotID);
	
		if (snapshot == null) // TODO: Implementar Exc
			throw new Exception("No se encontró snapshot con id: " + snapshotID);
		
		Network network = snapshot.getNetwork();
		Validator validator = network.getValidator();
		
		DiagnoseResult result = new DiagnoseResult();
				
		FacetQuery facetQuery = new SimpleFacetQuery(new SimpleStringCriteria( RecordValidationResult.SNAPSHOT_ID_FIELD + ":" + snapshotID));
	
		
		for (String fqTerm : fq) {
			fqTerm = fqTerm.replace("@@", ":");
			facetQuery.addFilterQuery( new SimpleFilterQuery(new SimpleStringCriteria(fqTerm)) );
		}
		
		facetQuery.setRows(0);
		
		FacetOptions facetOptions = new FacetOptions();
		facetOptions.setFacetMinCount(1);
		facetOptions.setFacetLimit(1000);
		
		for (String facetName: RecordValidationResult.FACET_FIELDS)  
			facetOptions.addFacetOnField(facetName);
		
		facetQuery.setFacetOptions(facetOptions);
		
		// Consulta SOLR
		FacetPage<RecordValidationResult> facetResult = solrTemplate.queryForFacetPage(facetQuery, RecordValidationResult.class);

		
		Map<String,Integer> validRuleMap = obtainFacetMap( facetResult.getFacetResultPage("valid_rules").getContent() );
		Map<String,Integer> invalidRuleMap = obtainFacetMap( facetResult.getFacetResultPage("invalid_rules").getContent() );
		Map<String,Integer> validRecordMap = obtainFacetMap( facetResult.getFacetResultPage("record_is_valid").getContent() );
		Map<String,Integer> transformedRecordMap = obtainFacetMap( facetResult.getFacetResultPage("record_is_transformed").getContent() );
		
		result.size = (int) facetResult.getTotalElements();
		result.validSize = 0;
		result.transformedSize = 0;
		
		if ( validRecordMap.get("true") != null )
			result.validSize = validRecordMap.get("true");
		
		if ( transformedRecordMap.get("true") != null )
			result.transformedSize = transformedRecordMap.get("true");

		for (String facetName: RecordValidationResult.FACET_FIELDS)  
			result.facets.put(facetName,  facetResult.getFacetResultPage(facetName).getContent() );
		
		
		for (ValidatorRule rule : validator.getRules() ) {
			
			String ruleID = rule.getId().toString();
			
			DiagnoseRuleResult ruleResult = new DiagnoseRuleResult();
			
			result.ruleNameByID.put(ruleID, rule.getName() );
			
			ruleResult.ruleID = rule.getId();
			ruleResult.validCount = validRuleMap.get(ruleID);
			ruleResult.invalidCount = invalidRuleMap.get(ruleID);
			ruleResult.name = rule.getName();
			ruleResult.description = rule.getDescription();
			ruleResult.mandatory = rule.getMandatory(); 
			
			result.rules.add(ruleResult);
			
		}		
		
		return result;
	}
	
	/**
	 * Retorna un Map entre los ids y los nombres de las reglas
	 */
	private Map<String,Integer> obtainFacetMap( List<FacetFieldEntry> facetList ) {
		
		Map<String,Integer> facetMap = new HashMap<String, Integer>();
		
		for ( FacetFieldEntry entry : facetList ) 
			facetMap.put( entry.getValue(), (int) entry.getValueCount() );
		
		return facetMap;
	}
	
	@Getter
	@Setter
	class DiagnoseResult {	
		
		public DiagnoseResult() {
			rules = new ArrayList<DiagnoseRuleResult>();
			facets = new HashMap<String, List<FacetFieldEntry>>();
			ruleNameByID = new HashMap<String, String>(); 
		}
		
		Integer size;
		Integer transformedSize;
		Integer validSize;;
		List<DiagnoseRuleResult> rules;
		Map<String, String> ruleNameByID;
		Map<String, List<FacetFieldEntry>> facets;
	}
	
	@Getter
	@Setter
	class DiagnoseRuleResult {
		Long ruleID;
		String name;
		String description;
		Boolean mandatory;
		Integer validCount;
		Integer invalidCount;
	}
	
	
	@RequestMapping(value = "/public/diagnoseValidationOcurrences/{snapshotID}/{ruleID}/[{fq}]", method = RequestMethod.GET)
	@ResponseBody
	public ValidationOccurrencesResult diagnoseValidationOcurrences(@PathVariable Long snapshotID, @PathVariable Long ruleID, @PathVariable List<String> fq)
			throws Exception {

		NetworkSnapshot snapshot = networkSnapshotRepository.findOne(snapshotID);
	
		if (snapshot == null) // TODO: Implementar Exc
			throw new Exception("No se encontró snapshot con id: " + snapshotID);
	
		ValidationOccurrencesResult result = new ValidationOccurrencesResult();
	
		FacetQuery facetQuery = new SimpleFacetQuery(new SimpleStringCriteria( RecordValidationResult.SNAPSHOT_ID_FIELD + ":" + snapshotID));
		facetQuery.setRows(0);
		
		for (String fqTerm : fq) {
			fqTerm = fqTerm.replace("@@", ":");
			facetQuery.addFilterQuery( new SimpleFilterQuery(new SimpleStringCriteria(fqTerm)) );
		}
		
		FacetOptions facetOptions = new FacetOptions();
		facetOptions.setFacetMinCount(1);
		facetOptions.setFacetLimit(1000);
		
		facetOptions.addFacetOnField(ruleID.toString() + RecordValidationResult.INVALID_RULE_SUFFIX );
		facetOptions.addFacetOnField(ruleID.toString() + RecordValidationResult.VALID_RULE_SUFFIX);
		
		facetOptions.setFacetSort( FacetSort.COUNT );
	
		facetQuery.setFacetOptions(facetOptions);
		
		FacetPage<RecordValidationResult> facetResult = solrTemplate.queryForFacetPage(facetQuery, RecordValidationResult.class);
		
		List<OccurrenceCount> validRuleOccurrence = new ArrayList<OccurrenceCount>();
		List<OccurrenceCount> invalidRuleOccurrence = new ArrayList<OccurrenceCount>();

		for ( FacetFieldEntry occr : facetResult.getFacetResultPage(ruleID.toString() + RecordValidationResult.VALID_RULE_SUFFIX).getContent() ) 
			validRuleOccurrence.add( new OccurrenceCount(occr.getValue(), (int) occr.getValueCount()) );
		
		for ( FacetFieldEntry occr : facetResult.getFacetResultPage(ruleID.toString() + RecordValidationResult.INVALID_RULE_SUFFIX ).getContent() ) 
			invalidRuleOccurrence.add( new OccurrenceCount(occr.getValue(), (int) occr.getValueCount()) );
		
		
		result.setValidRuleOccrs( validRuleOccurrence );
		result.setInvalidRuleOccrs( invalidRuleOccurrence );
	
		return result;
	}
	
	@Getter
	@Setter
	class OccurrenceCount {
		public OccurrenceCount(String value, int count) {
			super();
			this.value = value;
			this.count = count;
		}
		String value;
		Integer count;
	}
	
	@Getter
	@Setter
	class ValidationOccurrencesResult {
		List<OccurrenceCount> invalidRuleOccrs;
		List<OccurrenceCount> validRuleOccrs;	
	}
	
	
	@RequestMapping(value = "/public/diagnoseListRecordValidationResults/{snapshotID}/[{fq}]", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Page<RecordValidationResult>> diagnoseListRecordValidationResults(@PathVariable Long snapshotID, @PathVariable List<String> fq, Pageable pageable) {
		
		Query query = new SimpleQuery( RecordValidationResult.SNAPSHOT_ID_FIELD + ":" + snapshotID.toString() );
		
		// Esta correccion permite pagiona
		pageable = pageable.previousOrFirst();
		
		query.setPageRequest(pageable);
		
		for (String fqTerm : fq) {
			fqTerm = fqTerm.replace("@@", ":");
			query.addFilterQuery( new SimpleFilterQuery(new SimpleStringCriteria(fqTerm)) );
		}
	
		Page<RecordValidationResult> results = solrTemplate.queryForPage(query, RecordValidationResult.class);
		
		
		return new ResponseEntity<Page<RecordValidationResult>>(results, HttpStatus.OK);
	  }
	

	/*****************************************************************
	 * Validation Services
	 *****************************************************************/

	@ResponseBody
	@RequestMapping(value = "/public/listValidatorRulesByNetworkID/{id}", method = RequestMethod.GET)
	public List<ValidatorRule> listValidatorRulesByNetworkID(
			@PathVariable Long id) throws Exception {

		Network network = networkRepository.findOne(id);
		if (network == null)
			throw new Exception("No se encontró RED");

		return network.getValidator().getRules();

	}

	/*************************** Acciones Globales ****************************************/
	@ResponseBody
	@RequestMapping(value = "/private/networkAction/{action}/{ids}", method = RequestMethod.GET)
	public ResponseEntity<String> networkAction(
			@PathVariable NetworkAction action, @PathVariable Long... ids) {

		List<Long> networkIdsWithErrors = new ArrayList<Long>();

		for (Long id : ids) {

			// Se obtiene la red en cuestión
			Network network = networkRepository.findOne(id);

			// Si la red no existe se agrega a la list de redes con error
			if (network == null)
				networkIdsWithErrors.add(id);

			// De acuerdo a la acción se aplica sobre la red
			switch (action) {

			case START_HARVESTING:
				snapshotManager.lauchHarvesting(id);
				break;

			case START_HARVESTING_BYSET:
				snapshotManager.lauchSetBySetHarvesting(id);
				break;

			case STOP_HARVESTING:

				// detiene todos los snapshots que estén en status harvesting
				for (NetworkSnapshot snapshot : networkSnapshotRepository
						.findByNetworkAndStatus(network,
								SnapshotStatus.HARVESTING)) {
					snapshotManager.stopHarvesting(snapshot.getId());
				}
				break;

			case CLEAN_NETWORK:

				// obtiene el lgk para no borrarlo
				Long lgkSnapshotID = networkSnapshotRepository
						.findLastGoodKnowByNetworkID(id).getId();

				// recorre los snapshots no borrados
				for (NetworkSnapshot snapshot : networkSnapshotRepository
						.findByNetworkAndDeleted(network, false)) {

					// si no es el lgk
					if (snapshot.getId() != lgkSnapshotID)
						cleanSnapshot(snapshot);
				}
				break;

			case ADD_VUFIND_INDEX:
				try {
					NetworkSnapshot snapshot = networkSnapshotRepository
							.findLastGoodKnowByNetworkID(id);
					if (snapshot == null)
						throw new Exception(
								"Indexación Vufind fallida - No existe LGK para la red ID:"
										+ id);

					indexerManager.indexNetworkInVufind(id);
				} catch (Exception e1) {
					networkIdsWithErrors.add(id);
				}
				break;

			case ADD_XOAI_INDEX:
				try {
					NetworkSnapshot snapshot = networkSnapshotRepository
							.findLastGoodKnowByNetworkID(id);
					if (snapshot == null)
						throw new Exception(
								"Indexación XOAI fallida - No existe LGK para la red ID:"
										+ id);
					indexerManager.indexNetworkInXOAI(id);
				} catch (Exception e1) {
					networkIdsWithErrors.add(id);
				}
				break;

			case DELETE_VUFIND_INDEX:
				try {
					indexerManager.deleteNetworkFromVufind(id);
				} catch (Exception e1) {
					networkIdsWithErrors.add(id);
				}
				break;

			case DELETE_XOAI_INDEX:
				try {
					indexerManager.deleteNetworkFromXOAI(id);
				} catch (Exception e1) {
					networkIdsWithErrors.add(id);
				}
				break;

			case DELETE_NETWORK:
				try {
					deleteNetwork(network);
				} catch (Exception e) {
					networkIdsWithErrors.add(id);
				}

				break;

			default:
				break;
			}

			System.out.println(action + ".." + id);
		}

		return null;
	}

	/***** Acciones Auxiliares */

	private void cleanSnapshot(NetworkSnapshot snapshot) {

		System.out.println("Limpiando Snapshot: " + snapshot.getId());

		// TODO: Falta borrar el índice de solr de estadísticas

		// borra los resultados de validación
		System.out.println("Borrando registros de validaciones");

		// borra las estadisticas
		System.out.println("Borrando stadísticas de metadatos");

		// borra el log de cosechas
		System.out.println("Borrando registros de log");
		networkSnapshotLogRepository.deleteBySnapshotID(snapshot.getId());

		// borra los registros
		System.out.println("Borrando registros de metadatos");
		recordRepository.deleteBySnapshotID(snapshot.getId());

		// marcando snapshot borrado
		snapshot.setDeleted(true);
		networkSnapshotRepository.save(snapshot);
	}

	private void deleteSnapshot(NetworkSnapshot snapshot) {
		cleanSnapshot(snapshot);
		networkSnapshotRepository.delete(snapshot);
	}

	private void deleteNetwork(Network network) throws Exception {

		System.out.println("Comenzando proceso de borrando Red: "
				+ network.getName());

		indexerManager.deleteNetworkFromVufind(network.getId());
		indexerManager.deleteNetworkFromXOAI(network.getId());

		for (NetworkSnapshot snapshot : network.getSnapshots()) {
			deleteSnapshot(snapshot);
		}

		System.out.println("Borrando Origenes/Sets");

		for (OAIOrigin origin : network.getOrigins()) {
			setRepository.deleteInBatch(origin.getSets());
		}

		originRepository.deleteInBatch(network.getOrigins());

		networkRepository.delete(network);

		System.out.println("Finalizando borrado red: " + network.getName());
	}

	/*************************** Fin de acciones globales *******************************************************/

	/**************************** FrontEnd ************************************/

	@ResponseBody
	@RequestMapping(value = "/public/lastGoodKnowSnapshotByNetworkID/{id}", method = RequestMethod.GET)
	public ResponseEntity<NetworkSnapshot> getLGKSnapshot(@PathVariable Long id) {

		NetworkSnapshot snapshot = networkSnapshotRepository
				.findLastGoodKnowByNetworkID(id);
		ResponseEntity<NetworkSnapshot> response = new ResponseEntity<NetworkSnapshot>(
				snapshot, snapshot == null ? HttpStatus.NOT_FOUND
						: HttpStatus.OK);
		return response;
	}

	@ResponseBody
	@RequestMapping(value = "/public/getSnapshotByID/{id}", method = RequestMethod.GET)
	public ResponseEntity<NetworkSnapshot> getSnapshotByID(@PathVariable Long id) {

		NetworkSnapshot snapshot = networkSnapshotRepository.findOne(id);
		ResponseEntity<NetworkSnapshot> response = new ResponseEntity<NetworkSnapshot>(
				snapshot, snapshot == null ? HttpStatus.NOT_FOUND
						: HttpStatus.OK);
		return response;
	}

	@ResponseBody
	@RequestMapping(value = "/public/getSnapshotInfoByID/{id}", method = RequestMethod.GET)
	public NetworkInfo getSnapshotInfoByID(@PathVariable Long id)
			throws Exception {

		NetworkSnapshot snapshot = networkSnapshotRepository.findOne(id);

		if (snapshot == null) // TODO: Implementar Exc
			throw new Exception("No se encontró snapshot con id: " + id);

		Network network = snapshot.getNetwork();

		NetworkInfo ninfo = new NetworkInfo();
		ninfo.networkID = network.getId();
		ninfo.acronym = network.getAcronym();
		ninfo.name = network.getName();

		ninfo.snapshotID = snapshot.getId();
		ninfo.datestamp = snapshot.getEndTime();
		ninfo.size = snapshot.getSize();
		ninfo.validSize = snapshot.getValidSize();
		ninfo.transformedSize = snapshot.getTransformedSize();

		return ninfo;
	}

	@ResponseBody
	@RequestMapping(value = "/public/lastGoodKnowSnapshotByNetworkAcronym/{acronym}", method = RequestMethod.GET)
	public ResponseEntity<NetworkSnapshot> getLGKSnapshot(
			@PathVariable String acronym) throws Exception {

		Network network = networkRepository.findByAcronym(acronym);
		if (network == null) // TODO: Implementar Exc
			throw new Exception("No se encontró RED: " + acronym);

		NetworkSnapshot snapshot = networkSnapshotRepository
				.findLastGoodKnowByNetworkID(network.getId());
		if (snapshot == null) // TODO: Implementar Exc
			throw new Exception("No se encontró snapshot válido de la RED: "
					+ acronym);

		ResponseEntity<NetworkSnapshot> response = new ResponseEntity<NetworkSnapshot>(
				snapshot, snapshot == null ? HttpStatus.NOT_FOUND
						: HttpStatus.OK);

		return response;
	}

	@ResponseBody
	@RequestMapping(value = "/public/listSnapshotsByNetworkAcronym/{acronym}", method = RequestMethod.GET)
	public ResponseEntity<List<NetworkSnapshot>> listSnapshotsByAcronym(
			@PathVariable String acronym) throws Exception {

		Network network = networkRepository.findByAcronym(acronym);
		if (network == null)
			throw new Exception("No se encontró RED: " + acronym);

		ResponseEntity<List<NetworkSnapshot>> response = new ResponseEntity<List<NetworkSnapshot>>(
				networkSnapshotRepository
						.findByNetworkOrderByEndTimeAsc(network),
				HttpStatus.OK);

		return response;
	}

	// //////////////////////// Listar Redes y sus datos
	// //////////////////////////

	private List<NetworkInfo> networkList2netinfoList(List<Network> networks) {

		List<NetworkInfo> ninfoList = new ArrayList<NetworkInfo>();

		for (Network network : networks) {

			NetworkInfo ninfo = new NetworkInfo();
			ninfo.networkID = network.getId();
			ninfo.acronym = network.getAcronym();
			ninfo.name = network.getName();
			ninfo.institution = network.getInstitutionName();

			NetworkSnapshot lstSnapshot = networkSnapshotRepository
					.findLastByNetworkID(network.getId());
			if (lstSnapshot != null) {

				ninfo.lstSnapshotID = lstSnapshot.getId();
				ninfo.lstSnapshotDate = lstSnapshot.getEndTime();
				ninfo.lstSize = lstSnapshot.getSize();
				ninfo.lstValidSize = lstSnapshot.getValidSize();
				ninfo.lstTransformedSize = lstSnapshot.getTransformedSize();
				ninfo.lstSnapshotStatus = lstSnapshot.getStatus();

			}

			NetworkSnapshot lgkSnapshot = networkSnapshotRepository
					.findLastGoodKnowByNetworkID(network.getId());
			if (lgkSnapshot != null) {

				ninfo.snapshotID = lgkSnapshot.getId();
				ninfo.datestamp = lgkSnapshot.getEndTime();
				ninfo.size = lgkSnapshot.getSize();
				ninfo.validSize = lgkSnapshot.getValidSize();
				ninfo.transformedSize = lgkSnapshot.getTransformedSize();

				ninfo.lgkSnapshotID = lgkSnapshot.getId();
				ninfo.lgkSnapshotDate = lgkSnapshot.getEndTime();
				ninfo.lgkSize = lgkSnapshot.getSize();
				ninfo.lgkValidSize = lgkSnapshot.getValidSize();
				ninfo.lgkTransformedSize = lgkSnapshot.getTransformedSize();

			}
			ninfoList.add(ninfo);
		}
		return ninfoList;
	}

	// listado de redes publicadas
	@ResponseBody
	@RequestMapping(value = "/public/listNetworks", method = RequestMethod.GET)
	public ResponseEntity<List<NetworkInfo>> listNetworks() {

		List<NetworkInfo> ninfoList = networkList2netinfoList(networkRepository
				.findByPublishedOrderByNameAsc(true));
		return new ResponseEntity<List<NetworkInfo>>(ninfoList, HttpStatus.OK);
	}

	// listado de todas las redes
	@ResponseBody
	@RequestMapping(value = "/public/networks", method = RequestMethod.GET)
	public ResponseEntity<NetworksListResponse> listNetworks(
			@RequestParam Map<String, String> params) {

		NetworksListResponse response = new NetworksListResponse(
				findByParams(params));
		return new ResponseEntity<NetworksListResponse>(response, HttpStatus.OK);
	}

	private Page<Network> findByParams(Map<String, String> params) {

		int page = Integer.parseInt(params.get("page"));
		// Correción para que la paginación empiece en 1
		page--;

		int count = Integer.parseInt(params.get("count"));

		Pageable pageRequest = new PageRequest(page, count);

		String filterColumn = null;
		String filterExpression = null;

		Pattern sortingPattern = Pattern.compile("sorting\\[(.*)\\]");
		Pattern filterPattern = Pattern.compile("filter\\[(.*)\\]");

		// Matcher matcher = pattern.matcher(ISBN);

		for (String key : params.keySet()) {

			if (key.startsWith("sorting")) {

				Direction sortDirection = Direction.ASC;
				Matcher matcher = sortingPattern.matcher(key);

				if (matcher.find()) {
					String columnName = matcher.group(1);
					if (params.get(key).equals("desc"))
						sortDirection = Direction.DESC;
					pageRequest = new PageRequest(page, count, sortDirection,
							columnName);
				}
			}

			if (key.startsWith("filter")) {

				Matcher matcher = filterPattern.matcher(key);

				if (matcher.find()) {
					filterColumn = matcher.group(1);
					filterExpression = params.get(key);
				}
			}
		}

		if (filterColumn != null) {

			switch (filterColumn) {

			case "name":
				return networkRepository.findByNameIgnoreCaseContaining(
						filterExpression, pageRequest);

			case "institution":
				return networkRepository
						.findByInstitutionNameIgnoreCaseContaining(
								filterExpression, pageRequest);

			case "acronym":
				return networkRepository.findByAcronymIgnoreCaseContaining(
						filterExpression, pageRequest);

			default:
				return networkRepository.findAll(pageRequest);

			}
		} else
			return networkRepository.findAll(pageRequest);

	}

	@ResponseBody
	@RequestMapping(value = "/public/listNetworksHistory", method = RequestMethod.GET)
	public ResponseEntity<List<NetworkHistory>> listNetworksHistory() {

		List<Network> allNetworks = networkRepository
				.findByPublishedOrderByNameAsc(true);// OrderByName();
		List<NetworkHistory> NHistoryList = new ArrayList<NetworkHistory>();

		for (Network network : allNetworks) {
			NetworkHistory nhistory = new NetworkHistory();
			nhistory.name = network.getName();
			nhistory.networkID = network.getId();
			nhistory.acronym = network.getAcronym();
			nhistory.validSnapshots = networkSnapshotRepository
					.findByNetworkAndStatusOrderByEndTimeAsc(network,
							SnapshotStatus.VALID);
			NHistoryList.add(nhistory);
		}

		ResponseEntity<List<NetworkHistory>> response = new ResponseEntity<List<NetworkHistory>>(
				NHistoryList, HttpStatus.OK);

		return response;
	}


	/************** Clases de retorno de resultados *******************/

	@Getter
	@Setter
	class NetworkInfo {
		public String acronym;
		private Long networkID;
		private String name;
		private String institution;

		// DEPRECATED
		/** Esto queda por legacy pero es depreacted **/
		private Long snapshotID;
		@JsonSerialize(using = JsonDateSerializer.class)
		private Date datestamp;
		private int size;
		private int validSize;
		private int transformedSize;
		/** fin deprecated **/

		private Long lgkSnapshotID;
		@JsonSerialize(using = JsonDateSerializer.class)
		private Date lgkSnapshotDate;
		private int lgkSize;
		private int lgkValidSize;
		private int lgkTransformedSize;

		private Long lstSnapshotID;
		@JsonSerialize(using = JsonDateSerializer.class)
		private Date lstSnapshotDate;
		private SnapshotStatus lstSnapshotStatus;
		private int lstSize;
		private int lstValidSize;
		private int lstTransformedSize;
	}

	@Getter
	@Setter
	class NetworkHistory {
		public String name;
		public String acronym;
		private Long networkID;
		private List<NetworkSnapshot> validSnapshots;
	}

	@Getter
	@Setter
	class NetworksListResponse {

		private long totalElements;
		private int totalPages;
		private int pageNumber;
		private int pageSize;
		private List<NetworkInfo> networks;

		public NetworksListResponse(Page<Network> page) {
			super();
			this.networks = networkList2netinfoList(page.getContent());
			this.totalElements = page.getTotalElements();
			this.totalPages = page.getTotalPages();
			this.pageNumber = page.getNumber();
			this.pageSize = page.getSize();
		}
	}

}
