package org.galaxy

import extract.excel.ExcelBuilder
import org.spock.EditDistance

/**
 * Created by cjl7959 on 5/14/18.
 */
class Roster {

  List<Player> calSouthPlayers = []
  List<Player> galaxyPlayers = []  //only used in update mode

  private static final String GALAXY_GIRLS_SOUTH_FILENAME = 'GalaxyGirlsSouth2018.csv'
  private static final String GALAXY_GIRLS_NORTH_FILENAME = 'GalaxyGirlsNorth2018.csv'
  private static final String GALAXY_BOYS_NORTH_FILENAME = 'GalaxyBoysNorth2018.csv'
  private static final String GALAXY_BOYS_SOUTH_FILENAME = 'GalaxyBoysSouth2018.csv'
  private static final String SOUTH = 'South'
  private static final String NORTH = 'North'
  private static final String GIRL = 'Girls'
  private static final String BOY = 'Boys'
  private static String RUN_MODE
  private static String GALAXY_HEADERS = 'Name,Birth Year,Last Years Ranking,Buddy Request,School,Parent Name,Cell,Email,Last Name,First Name,Gender,Birth Date,Division,Calsouth Player Id\n'

  /**
   * Generate 4 division rosters as CSV files from calsouth excel spreadsheet.
   *    New mode will generate all calSouthPlayers from Cal South
   *    Update mode will only append calSouthPlayers not includes in 4 division excel files
   *
   * @param args
   * arg0=mode new/update
   * arg1==CalSouthExcelFileName
   * arg2=GalaxyBoysSouthExcelFileName  in update mode
   * arg3=GalaxyBoysNorthExcelFileName in update mode
   * arg4=GalaxyGirlsSouthExcelFileName in update mode
   * arg5=GalaxyGirlNorthExcelFileName' in update mode

   */
  static void main(String[] args) {
    println("Converting CalSouth file to Roster");

    String runMode

    if (args.size() < 1) {
      println 'wrong number of arguments. arg0=update/new arg1=CalSouthExcelFileName'
      println 'arg2=GalaxyBoysSouthExcelFileName, arg3=GalaxyBoysNorthExcelFileName, arg4=GalaxyGirlsSouthExcelFileName, arg5=GalaxyGirlNorthExcelFileName'
      return
    }
    runMode = args[0]

    File inputExcelFile = new File(args[1])
    if (!inputExcelFile.exists()) {
      println "calsouth input file not found"
      return
    }
    println "Input calsouth filename: ${inputExcelFile.name}"

    Roster roster = new Roster()
    roster.processRoster(inputExcelFile.name, runMode, args)
  }

  protected void processRoster(String inputExcelFileName, String runMode, String[] runArgs) {
    calSouthPlayers = getCalSouthPlayers(inputExcelFileName)
    if (runMode == 'update') {
      List<Player> existingGalaxyPlayers = getExistingGalaxyPlayers(runArgs)
      galaxyPlayers = addCalSouthPlayers (existingGalaxyPlayers, calSouthPlayers)
      writeRosterToCsv(galaxyPlayers)
    } else {
      writeRosterToCsv(calSouthPlayers)
    }
  }

  protected  List<Player> getExistingGalaxyPlayers(String[] runArgs) {
    String galaxyBoysSouthExcelFileName = runArgs[2]
    String galaxyBoysNorthExcelFileName = runArgs[3]
    String galaxyGirlsSouthExcelFileName = runArgs[4]
    String galaxyGirlsNorthExcelFileName = runArgs[5]

    List<Player> players = getExistingGalaxyPlayersFromFile(galaxyBoysSouthExcelFileName, SOUTH, BOY) +
        getExistingGalaxyPlayersFromFile(galaxyBoysNorthExcelFileName, NORTH, BOY) +
        getExistingGalaxyPlayersFromFile(galaxyGirlsSouthExcelFileName, SOUTH, GIRL) +
        getExistingGalaxyPlayersFromFile(galaxyGirlsNorthExcelFileName, NORTH, BOY)
    players
  }

  protected List<Player> addCalSouthPlayers (List<Player> existingPlayers, List<Player> newPlayers) {
    List<Player> players = existingPlayers
    newPlayers.each { newPlayer ->
        Player matchPlayer = existingPlayers.find {existingPlayer ->
          "${newPlayer.firstName} ${newPlayer.lastName}".toString() ==  existingPlayer.fullName &&
              newPlayer.birthYear == existingPlayer.birthYear &&
              newPlayer.division == existingPlayer.division
      }
      if (!matchPlayer) {
        players << newPlayer //only add new player
      }
    }
    players
  }

  protected List<Player> getExistingGalaxyPlayersFromFile(String galaxyExcelFileName, String division, String gender) {
    List<Player> players = []
      new ExcelBuilder(galaxyExcelFileName).eachLine([labels: true]) {
        players << new Player().with {  //todo
         // lastName = Last_Name
         // firstName = First_Name
          fullName = $Name
          buddyRequest = Buddy_Request
          schoolName = School
          //birthDate = getBirthDate(dob)
          birthYear = Birth_Year
          parentName = Parent_Name
          it.gender = gender
          it.email = Email
          mobilePhone = Cell
          it.division = division
          //playerId = Player_ID
          //ageGroup = Age_Group
          it
        }
        players.each { println it }
      }
    players
  }


  Date getBirthDate(String dob) {
    println dob
    String longDateFormat = 'EEE MMM dd HH:mm:ss z yyyy'
    if (dob) {
      if (dob.length() == 28) {
        return new Date().parse('EEE MMM dd HH:mm:ss z yyyy', dob)
      } else if (dob.length() == 10) {  //default
        new Date().parse('MM/dd/yyyy', dob)
      }
    } else { return null }
  }

  List<Player> getCalSouthPlayers(String inputExcelFileName) {
    List<Player> players = []
    new ExcelBuilder(inputExcelFileName).eachLine([labels: true]) {
      String dob = DOB
      players << new Player().with {
        lastName = Last_Name
        firstName = First_Name
        fullName = "$firstName $lastName"
        buddyRequest = Buddy_Request
        schoolName = School_Name
        birthDate = getBirthDate(dob)
        birthYear = birthDate ? birthDate.format('yyyy') : null
        parentName = Parent1_Name
        it.gender = Gender
        it.email = Email
        mobilePhone = Cell_Phone
        it.division = Division
        playerId = Player_ID
        ageGroup = Age_Group as String
        it
      }
    }
    players
  }

  void validatePlayer() {
  }

  /**
   * Similarity in percentage between two strings
   * @param value1
   * @param value2
   * @return
   */
  Integer similarityPercentage(String value1, String value2) {
    new EditDistance(value1, value2).similarityInPercent
  }

  /**
   * Write roster to 4 divisions boys-south, boys-north, girls-north, and girls south
   */
  void writeRosterToCsv(List<Player> galaxyPlayers) {
    println 'writing csv files'
    Closure writePlayer = { writer, player ->
      if (player.fullName && player.birthYear) {
        player.with {
          writer.write("$fullName,${birthYear ?: ''},$lastYearRanking,$buddyRequest,$schoolName,$parentName,$mobilePhone,$email,$lastName,$firstName,$gender,${birthDate.format('MM/dd/yyyy')},$division,$playerId\n")
        }
      }
    }
    BufferedWriter girlsNorthWriter = new File(GALAXY_GIRLS_NORTH_FILENAME).newWriter()
    BufferedWriter boysNorthWriter = new File(GALAXY_BOYS_NORTH_FILENAME).newWriter()
    BufferedWriter girlsSouthWriter = new File(GALAXY_GIRLS_SOUTH_FILENAME).newWriter()
    BufferedWriter boysSouthWriter = new File(GALAXY_BOYS_SOUTH_FILENAME).newWriter()

    girlsNorthWriter.write(GALAXY_HEADERS)
    boysNorthWriter.write(GALAXY_HEADERS)
    girlsSouthWriter.write(GALAXY_HEADERS)
    boysSouthWriter.write(GALAXY_HEADERS)

    galaxyPlayers.each { player ->
      if (player.division == NORTH && player.gender == GIRL) {
        writePlayer(girlsNorthWriter, player)
      } else if (player.division == NORTH && player.gender == BOY) {
        writePlayer(boysNorthWriter, player)
      } else if (player.division == SOUTH && player.gender == GIRL) {
        writePlayer(girlsSouthWriter, player)
      } else if (player.division == SOUTH && player.gender == BOY) {
        writePlayer(boysSouthWriter, player)
      }
    println player
    }
    boysNorthWriter.close()
    boysSouthWriter.close()
    girlsNorthWriter.close()
    girlsSouthWriter.close()
  }
}

