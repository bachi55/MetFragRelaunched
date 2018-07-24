#' @import rcdk
#' @import rJava
NULL

.packageName <- "metfRag"

require(rJava, quietly=TRUE)

.onLoad<-function(libname, pkgname) {
    jar.metfrag <- paste(libname, pkgname, "java", "MetFragR-2.3.1-jar-with-dependencies.jar", sep=.Platform$file.sep)
    .jinit(classpath=c(jar.metfrag))
}

#' Retrieve candidatesas according to the specifications in the settings.
#' 
#' The function uses defined settings for the database and retrieval to download
#' the molecular candidates from the specified database. 
#' 
#' @param list of parameter settings
#' @author Eric Bach (\email{eric.bach@aalto.fi})
#' @export
run.candidateRetrieval<-function(settingsObject) {

    if(missing(settingsObject)) stop("Error: Settings object is missing!")
    
    if(class(settingsObject) != "list") stop("Error: Settings object must be of type list!")
    if(is.null(names(settingsObject))) stop("Error: Settings object does not contain valid names!")
    if(length(settingsObject) == 0) stop("Error: Settings object does not contain valid values!")
    
    getDatatype<-function(name, value) {
        vector = FALSE;
        if(class(value) != "character" & length(value) > 1) {vector = TRUE}
        if(name == "NeutralPrecursorMass") {return("double")}
        else if(name == "KeggProxyPort") {return("integer")}
        else if(name == "MoNAProxyPort") {return("integer")}
        else if(name == "MetaCycProxyPort") {return("integer")}
        else if(name == "PubChemProxyPort") {return("integer")}
        else if(name == "NeutralPrecursorMass") {return("double")}
        else if(name == "DatabaseSearchRelativeMassDeviation") {return("double")}
        else if(name == "FragmentPeakMatchAbsoluteMassDeviation") {return("double")}
        else if(name == "FragmentPeakMatchRelativeMassDeviation") {return("double")}
        else if(name == "MaximumTreeDepth") {return("integer")}
        else if(name == "PrecursorIonMode") {return("integer")}
        else if(name == "IonizedPrecursorMass") {return("double")}
        else if(name == "NumberThreads") {return("byte")}
        else if(name == "ExperimentalRetentionTimeValue") {return("double")}
        else if(name == "MinimumAbsolutePeakIntensity") {return("double")}
        else if(name == "SmartsSubstructureExclusionScoreSmartsList") {return("array")}
        else if(name == "SmartsSubstructureInclusionScoreSmartsList") {return("array")}
        else if(name == "ScoreSmartsInclusionList") {return("array")}
        else if(name == "ScoreSmartsExclusionList") {return("array")}
        else if(name == "FilterSmartsInclusionList") {return("array")}
        else if(name == "FilterSmartsExclusionList") {return("array")}
        else if(name == "FilterSuspectLists") {return("array")}
        else if(name == "ScoreSuspectLists") {return("array")}
        else if(name == "FilterExcludedElements") {return("array")}
        else if(name == "FilterIncludedElements") {return("array")}
        else if(name == "CombinedReferenceScoreValues") {return("array")}
        else if(name == "MetFragScoreWeights") {return("array_double")}
        else if(name == "MetFragPreProcessingCandidateFilter") {return("array")}
        else if(name == "MetFragPostProcessingCandidateFilter") {return("array")}
        else if(name == "PrecursorCompoundIDs") {return("array")}
        else if(name == "MetFragScoreTypes") {return("array_double")}
        else if(name == "MetFragCandidateWriter") {return("array")}
        else if(vector) {
            if(class(value) == "numeric" && value == round(value)) {return("array")}
            else if(class(value) == "numeric" && value != round(value)) {return("array_double")}
            else if(class(value) == "character") {return("array")}
            else if(class(value) == "logical") {return("array")}
            else {return("unknown")}
        }
        else if(!vector) {
            if(class(value) == "numeric" && value == round(value)) {return("integer")}
            else if(class(value) == "numeric" && value != round(value)) {return("double")}
            else if(class(value) == "character") {return("string")}
            else if(class(value) == "logical") {return("boolean")}
            else {return("unknown")}
        }
        
    }
    
    #write all properties into Java settings object
    javaSettings=.jnew("de/ipbhalle/metfraglib/settings/MetFragGlobalSettings")
    .jcall(javaSettings, "V", 'set', "PeakListString", "NA NA") # add empty spectra
    .jcall(javaSettings, "V", 'set', "MetFragPeakListReader", 
           .jnew("java.lang.String", 
                 as.character("de.ipbhalle.metfraglib.peaklistreader.FilteredStringTandemMassPeakListReader")))
    
    sapply(names(settingsObject), function(name) {
        #in case it a single value
        if(getDatatype(name, settingsObject[[name]]) == "integer") {
            .jcall(javaSettings, "V", 'set', name, 
                   .jnew("java.lang.Integer", as.integer(settingsObject[[name]])))
        }
        else if(getDatatype(name, settingsObject[[name]]) == "double") {
            .jcall(javaSettings, "V", 'set', name, 
                   .jnew("java.lang.Double", as.double(settingsObject[[name]])))
        }
        else if(getDatatype(name, settingsObject[[name]]) == "string") {
            .jcall(javaSettings, "V", 'set', name, 
                   .jnew("java.lang.String", as.character(settingsObject[[name]])))
        }
        else if(getDatatype(name, settingsObject[[name]]) == "boolean") {
            .jcall(javaSettings, "V", 'set', name, 
                   .jnew("java.lang.Boolean", as.logical(settingsObject[[name]])))
        }
        else if(getDatatype(name, settingsObject[[name]]) == "byte") {
            .jcall(javaSettings, "V", 'set', name, 
                   .jnew("java.lang.Byte", .jbyte(settingsObject[[name]])))
        }
        #vectors
        else if(getDatatype(name, settingsObject[[name]]) == "array") {
            .jcall(javaSettings, "V", 'set', name, 
                   .jarray(settingsObject[[name]]))
        }
        else if(getDatatype(name, settingsObject[[name]]) == "array_double") {
            .jcall(javaSettings, "V", 'set', name, 
                   .jarray(settingsObject[[name]], "[java.lang.Double"))
        }
        else {
            print(paste("Unknown type of parameter", name, "(", class(settingsObject[[name]]), ")"))
        }
    })
    
    # Get directory for temporary files
    temp_dir=tempdir()
    J("java.lang.System")$setProperty( "java.io.tmpdir", temp_dir )
    
    # Implement code from "de/ipbhalle/metfrag/r/MetfRag/runMetFrag" directly 
    # here.
    checker <- .jnew("de.ipbhalle.metfraglib.parameter.SettingsChecker")
    if(!.jcall(checker, "Z", "check",
               .jcast(javaSettings, "de.ipbhalle.metfraglib.settings.Settings"), FALSE)) {
        return(data.frame())
    }
    
    logger_strings <- c("net.sf.jnati.deploy.repository.ClasspathRepository",
                        "net.sf.jnati.deploy.artefact.ConfigManager",
                        "net.sf.jnati.deploy.repository.ClasspathRepository",
                        
                        "net.sf.jnati.deploy.repository.LocalRepository",
                        "net.sf.jnati.deploy.artefact.ManifestReader",
                        "net.sf.jnati.deploy.NativeArtefactLocator",
                        "net.sf.jnati.deploy.NativeLibraryLoader",
                        "net.sf.jnati.deploy.resolver.ArtefactResolver",
                        "net.sf.jnati.deploy.source.JarSource",
                        
                        "httpclient.wire.content",
                        "httpclient.wire.header",
                        
                        "org.apache.commons.httpclient.HeaderElement",
                        "org.apache.commons.httpclient.HttpConnection",
                        "org.apache.commons.httpclient.HttpMethodBase",
                        "org.apache.commons.httpclient.HttpClient",
                        "org.apache.commons.httpclient.util.IdleConnectionHandler",
                        "org.apache.commons.httpclient.MultiThreadedHttpConnectionManager",
                        "org.apache.commons.httpclient.HttpClient",
                        "org.apache.commons.httpclient.HeaderElement",
                        "org.apache.commons.httpclient.HttpParser",
                        "org.apache.commons.httpclient.methods.EntityEnclosingMethod",
                        "org.apache.commons.httpclient.methods.PostMethod",
                        "org.apache.commons.httpclient.methods.EntityEnclosingMethod",
                        "org.apache.commons.httpclient.cookie.CookieSpec",
                        "org.apache.commons.httpclient.HttpState",
                        "org.apache.commons.httpclient.methods.ExpectContinueMethod",
                        "org.apache.commons.httpclient.methods.EntityEnclosingMethod",
                        "org.apache.commons.httpclient.HttpMethodDirector",
                        "org.apache.commons.httpclient.params.DefaultHttpParams",
                        "org.apache.commons.httpclient.methods.PostMethod",
                        "org.apache.commons.httpclient.HttpParser",
                        "org.apache.commons.httpclient.methods.EntityEnclosingMethod",
                        
                        "org.apache.axis2.description.AxisOperation",
                        "org.apache.axiom.om.impl.llom.OMElementImpl",
                        "org.apache.axis2.engine.Phase",
                        "org.apache.axis2.transport.http.CommonsHTTPTransportSender",
                        "org.apache.axis2.context.ConfigurationContext",
                        "org.apache.axis2.addressing.AddressingHelper",
                        "org.apache.axis2.dispatchers.AddressingBasedDispatcher",
                        "org.apache.axis2.engine.AxisEngine",
                        "org.apache.axis2.builder.BuilderUtil",
                        "org.apache.axis2.transport.TransportUtils",
                        "org.apache.axis2.client.Options",
                        "org.apache.axis2.context.MessageContext",
                        "org.apache.axis2.transport.http.HTTPSender",
                        "org.apache.axis2.transport.http.SOAPMessageFormatter",
                        "org.apache.axis2.description.AxisOperation",
                        "org.apache.axis2.engine.AxisConfiguration",
                        "org.apache.axis2.description.OutInAxisOperationClient",
                        "org.apache.axis2.description.AxisService",
                        "org.apache.axis2.addressing.EndpointReference",
                        "org.apache.axis2.transport.http.AbstractHTTPSendere",
                        "org.apache.axis2.i18n.ProjectResourceBundle",
                        "org.apache.axis2.description.ParameterIncludeImpl",
                        "org.apache.axis2.deployment.ModuleBuilder",
                        "org.apache.axis2.handlers.addressing.AddressingInHandler",
                        "org.apache.axis2.deployment.DeploymentEngine",
                        "org.apache.axis2.deployment.ModuleDeployer",
                        "org.apache.axis2.transport.http.AbstractHTTPSender",
                        "org.apache.axis2.util.Loader",
                        "org.apache.axis2.deployment.RepositoryListener",
                        "org.apache.axis2.context.AbstractContext",
                        
                        "org.apache.axiom.om.impl.builder.StAXOMBuilder",
                        "org.apache.axiom.locator.DefaultOMMetaFactoryLocator",
                        "org.apache.axiom.om.util.StAXUtils",
                        "org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder",
                        "org.apache.axiom.om.impl.MTOMXMLStreamWriter",
                        "org.apache.axiom.om.OMOutputFormat",		
                        "org.apache.axiom.soap.impl.llom.SOAPEnvelopeImpl",
                        "org.apache.axiom.om.impl.llom.OMContainerHelper",
                        "org.apache.axiom.locator.ImplementationFactory",
                        "org.apache.axiom.om.impl.llom.factory.OMLinkedListMetaFactory",
                        "org.apache.axiom.locator.PriorityBasedOMMetaFactoryLocator",
                        "org.apache.axiom.locator.ImplementationFactory",
                        "org.apache.axiom.om.impl.common.AxiomContainerSupport",
                        "org.apache.axiom.om.impl.common.serializer.pull.PullSerializer",
                        "org.apache.axiom.om.impl.common.serializer.pull.Navigator",	
                        "org.apache.axiom.om.impl.builder.StAXBuilder")
    
    Level.ERROR <- J("org.apache.log4j.Level")$ERROR
    for (str in logger_strings) {
        .jcall(J("org.apache.log4j.Logger")$getLogger(str), "V", "setLevel", Level.ERROR)
    }
    
    log_level_name <- J("de.ipbhalle.metfraglib.parameter.VariableNames")$LOG_LEVEL_NAME
    Level.INFO <- J("org.apache.log4j.Level")$INFO
    .jcall(javaSettings, "V", "set", log_level_name, .jcast(Level.INFO, "java.lang.Object"))
    
    comb_metfrag_proc <- .jnew("de.ipbhalle.metfraglib.process.CombinedMetFragProcess", javaSettings)
    candidates_retrieved <- .jcall(comb_metfrag_proc, "Z", "retrieveCompounds", check = FALSE)
    if (! candidates_retrieved) {
        return(data.frame())
    }
    j_exception <- .jgetEx()
    if (.jcheck(silent = TRUE)) {
        if (verbose) {
            print(e)
        }
        return(data.frame())
    }

    candidateList <- .jcall(comb_metfrag_proc, 
                            "Lde/ipbhalle/metfraglib/list/CandidateList;", 
                            "getCandidateList")
    
    #remove axis temp dirs in case they were generated
    axis_files <- dir(temp_dir, full.names = TRUE)[grep("^axis2", dir(temp_dir))]
    axis_dirs <- axis_files[which(file.info(axis_files)[, "isdir"])]
    files_to_remove <- unlist(lapply(1:length(axis_dirs),
                                     function(x) dir(axis_dirs[x], full.names=TRUE)))
    files_to_remove <- files_to_remove[grep("jar$", files_to_remove)]
    unlink (files_to_remove)
    
    numberCandidates <- .jcall(candidateList, "I", "getNumberElements")
    
    if (numberCandidates == 0) {
        return(data.frame())
    }
    
    propertyNames<-c()
    datatypes<-list()
    
    if(numberCandidates >= 1) {
        candidate <- .jcall(candidateList, 
                            "Lde/ipbhalle/metfraglib/interfaces/ICandidate;",
                            "getElement", as.integer(0))
        propertyNames <- .jcall(candidate, "[S", "getPropertyNames")
        sapply(1:length(propertyNames), function(propertyIndex) {
            datatypes[[propertyNames[propertyIndex]]] <<- .jcall(
                candidate, "Ljava/lang/Object;",
                "getProperty", propertyNames[propertyIndex])$getClass()$getName()
        })
    }
    
    candidateProperties <- list()
    sapply(1:length(propertyNames), function(propertyIndex) {
        candidateProperties[[propertyNames[propertyIndex]]] <<- vector(mode = "character", length = 0)
    })
    sapply(1:numberCandidates, function(candidateIndex) {
        candidate <- .jcall(
            candidateList, "Lde/ipbhalle/metfraglib/interfaces/ICandidate;", 
            "getElement", as.integer(candidateIndex - 1))
        
        sapply(1:length(propertyNames), function(propertyIndex) {
            value <- .jcall(
                candidate, "Ljava/lang/Object;",
                "getProperty", propertyNames[propertyIndex])$toString()
            candidateProperties[[propertyNames[propertyIndex]]] <<- c(
                candidateProperties[[propertyNames[propertyIndex]]], value)
        })
    })
    
    sapply(1:length(propertyNames), function(propertyIndex) {
        datatype <- datatypes[[propertyNames[propertyIndex]]]
        if(datatype == "java.lang.Double" ||
           datatype == "java.lang.Byte" || 
           datatype == "java.lang.Integer" || 
           datatype == "java.lang.Float") {
            suppressWarnings(
                candidateProperties[[propertyNames[propertyIndex]]] <<- as.numeric(
                    candidateProperties[[propertyNames[propertyIndex]]]))
        }
    })
    
    return(as.data.frame(candidateProperties))
}
