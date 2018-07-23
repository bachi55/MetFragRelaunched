\name{run.candidateRetrieval}
\alias{run.candidateRetrieval}
\title{Retrieve candidatesas according to the specifications in the settings object provided as list.}
\usage{
    run.candidateRetrieval(settingsObject)
}
\arguments{
    \item{settingsObject}{A \code{list} of parameter settings}
}
\description{
    The function uses defined settings for the database and retrieval to download
    the molecular candidates from the specified database. 
}
\value{
    Returns a data.frame containing candidates represented by different properties and identifiers.
}
\author{
    Eric Bach (\email{eric.bach@aalto.fi})
}
\examples{
    #define settings
    settingsObject<-list()
    settingsObject[["DatabaseSearchRelativeMassDeviation"]]<-5.0
    settingsObject[["FragmentPeakMatchAbsoluteMassDeviation"]]<-0.001
    settingsObject[["FragmentPeakMatchRelativeMassDeviation"]]<-5.0
    settingsObject[["MetFragDatabaseType"]]<-"PubChem"
    settingsObject[["NeutralPrecursorMass"]]<-253.966126
    settingsObject[["NeutralPrecursorMolecularFormula"]]<-"C7H5Cl2FN2O3"
    settingsObject[["PrecursorCompoundIDs"]]<-c("50465", "57010914", "56974741", "88419651", "23354334")
    #define candidate filters
    settingsObject[["MetFragPreProcessingCandidateFilter"]]<-c("UnconnectedCompoundFilter","IsotopeFilter")
    settingsObject[["MetFragPostProcessingCandidateFilter"]]<-c("InChIKeyFilter")
    #run MetFrag
    candidates<-run.candidateRetrieval(settingsObject)
    #candidates is a data.frame with candidate properties
}
