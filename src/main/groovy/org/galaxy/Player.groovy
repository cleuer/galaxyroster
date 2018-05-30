package org.galaxy

import groovy.transform.ToString

/**
 * Created by cjl7959 on 5/10/18.
 */
@ToString(includeNames = true)
class Player {

  String fullName
  String birthYear
  String lastYearRanking
  String buddyRequest
  String schoolName
  String parentName
  String mobilePhone
  String email

  //additional columns
  String lastName
  String firstName
  String gender
  Date birthDate
  String ageGroup
  String division
  String playerId
  //todo: get more useful data

  @Override
  boolean equals (java.lang.Object other) {
  if (other == null) return false
  if (this.is(other)) return true
  if (Player != other.getClass()) return false
  if (lastName != other.lastName) return false
  if (firstName != other.firstName) return false
  if (birthYear != other.birthYear) return false
    return true
  }

  @Override
  int hashCode() {
    return 47
  }
}
