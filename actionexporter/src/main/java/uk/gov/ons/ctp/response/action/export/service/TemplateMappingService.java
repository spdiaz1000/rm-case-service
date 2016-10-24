package uk.gov.ons.ctp.response.action.export.service;

import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.action.export.domain.TemplateEngine;
import uk.gov.ons.ctp.response.action.export.domain.TemplateMappingDocument;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface TemplateMappingService {
  TemplateMappingDocument storeTemplateMappingDocument(String templateMappingName, TemplateEngine templateEngine,
                                                       InputStream fileContents) throws CTPException;
  TemplateMappingDocument retrieveTemplateMappingDocument(String templateMappingName);
  List<TemplateMappingDocument> retrieveAllTemplateMappingDocuments();

  // TODO rename, move?
  Map<String, String> retrieveMaoFromTemplateMappingDocument(String templateMappingName);
}