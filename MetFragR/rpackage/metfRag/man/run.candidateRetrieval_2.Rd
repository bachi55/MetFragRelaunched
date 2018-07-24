\name{run.candidateRetrieval_2}
\alias{run.candidateRetrieval_2}
\title{Retrieve candidatesas according to the specifications in the settings object provided as list.}
\usage{
    run.candidateRetrieval_2(settingsObject)
}
\arguments{
    \item{settingsObject}{A \code{list} of parameter settings}
}
\description{
    The function uses defined settings for the database and retrieval to download
    the molecular candidates from the specified database.
    
    This function uses the static method "runCandidateRetrieval" in the MetfRag.java file!
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
    #run MetFrag
    candidates<-run.candidateRetrieval_2(settingsObject)
    #candidates is a data.frame with candidate properties
}
