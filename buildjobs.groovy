def githubOrg = 'jmchilton'
def githubProject = 'jobs-dsl-test'
def baseJobName = 'community-tests-'

def engine = new groovy.text.SimpleTemplateEngine()

script_path = new File(__FILE__)
recipe_path = new File(script_path, 'recipes')

testShellTemplate = engine.createTemplate('make TEST_EXPRESSION="-k $testName"')


recipe_path.eachFile {
    def recipeName = it.name
    def jobName = "${baseJobName}${recipeName}"
    templateBinding = [
        "testName": recipeName
    ]
    job(jobName) {
        scm {
            git("git://github.com/${githubOrg}/${githubProject}.git")
        }
        steps {
            shell(testShellTemplate.make(templateBinding).toString())
        }
    }
}

