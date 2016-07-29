import groovy.json.JsonSlurper

def githubOrg = 'jmchilton'
def githubProject = 'jobs-dsl-test'
def baseJobName = 'community-tests-'

def engine = new groovy.text.SimpleTemplateEngine()

scriptPath = new File(__FILE__)
rootPath = scriptPath.getParentFile()
recipePath = new File(rootPath, 'recipes')

testShellTemplate = engine.createTemplate('make TEST_EXPRESSION="-k $testName"')


recipePath.eachFile {
    def recipeName = it.name
    def jobName = "${baseJobName}${recipeName}"
    def sout = new StringBuilder(), serr = new StringBuilder()
    def proc = "make dump-description RECIPE_NAME=${recipeName}".execute(null, rootPath)
    proc.consumeProcessOutput(sout, serr)
    proc.waitForOrKill(30000)
    println "out> $sout err> $serr"
    def defJsonString = new File(it, "def.json").text
    def jsonSlurper = new JsonSlurper()
    def recipeDef = jsonSlurper.parseText(defJsonString)
    templateBinding = [
        "testName": recipeName
    ]
    def shellCommand = testShellTemplate.make(templateBinding).toString()
    def jenkinsDescription = """<p>${recipeDef["description"]}</p>
<p>This test can be executed locally as follows:</p>
<code>
${shellCommand}
</code>
    """
    job(jobName) {
        description(jenkinsDescription)
        scm {
            git("git://github.com/${githubOrg}/${githubProject}.git")
        }
        steps {
            shell(shellCommand)
        }
    }
}

