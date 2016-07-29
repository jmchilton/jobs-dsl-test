import groovy.json.JsonSlurper

def githubOrg = 'jmchilton'
def githubProject = 'jobs-dsl-test'
def baseJobName = 'community-tests-'

def engine = new groovy.text.SimpleTemplateEngine()

script_path = new File(__FILE__)
recipePath = new File(script_path.getParentFile(), 'recipes')

testShellTemplate = engine.createTemplate('make TEST_EXPRESSION="-k $testName"')


recipePath.eachFile {
    def recipeName = it.name
    def jobName = "${baseJobName}${recipeName}"
    println "make dump-description RECIPE_NAME=${recipeName}".execute().text
    println "Test"
    def defJsonString = new File(it, "def.json").text
    def jsonSlurper = new JsonSlurper()
    def recipeDef = jsonSlurper.parseText(defJsonString)
    templateBinding = [
        "testName": recipeName
    ]
    job(jobName) {
        description(recipeDef["description"])
        scm {
            git("git://github.com/${githubOrg}/${githubProject}.git")
        }
        steps {
            shell(testShellTemplate.make(templateBinding).toString())
        }
    }
}

