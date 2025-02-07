package com.axiommd
import com.raquo.laminar.api.L.{* /*, given*/}
import com.axiom.model.shared.dto.Patient
import com.axiommd.shapeless.{Display}
object Model :
  type PatientFields = List[Display]

  val patientListVar = Var(List.empty[Patient])
  val colHeadersVar = Var(List.empty[String])
  val patientFieldEnums = Var(List[PatientFields]())

  def fetchPatients = ModelFetch.fetchPatients





