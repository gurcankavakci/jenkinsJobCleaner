import java.util.regex.*
import hudson.model.*
import jenkins.model.Jenkins
import hudson.FilePath

/**
 * Created with IntelliJ IDEA.
 * User: gkavakci
 * Date: 02.10.2017
 * Time: 14:34
 */


// Initialize debugMode parameter to TRUE if not given as script parameter
debugMode = true;
def limit = limitDay.toInteger()

if (debugMode == true) {
    println "** Execute a debugMode - no files will ever be deleted **";
}

// shortcut to Jenkins instance
def jenkins = jenkins.model.Jenkins.instance;

// Search for Projects without custom workspace and collect their name
def jobNames = jenkins.items.findAll { it instanceof hudson.model.Job && it.customWorkspace == null }.collect {
    it.name
};

def now = new Date()

//Which jobs will be delete
Pattern pattern = Pattern.compile("TMS.*(Compile|Rebuild)")

println("Limit: ${limit} days.")
println("Existing Jobs: ");
jobNames.each {
    println "-- $it --"
    def item = Jenkins.instance.getItem(it)
    def lastBuild = item.getLastBuild()
    if (lastBuild != null) {
        //println lastBuild.getTime().format("dd-MM-yyyy")

        def duration
        use(groovy.time.TimeCategory) {
            duration = now - lastBuild.getTime()
        }
        //println "days: ${duration.days}, Hours: ${duration.hours}"

        if (duration.days > limit) {
            println("Checking...")
            Matcher matcher = pattern.matcher(item.fullName)
            if (matcher.matches()) {
                if (debugMode == true) {
                    println " DELETE: ${item.fullName} (debugMode), last build older than ${limit} days"
                } else {
                    println "  DELETE: ${item.fullName} , last build older than ${limit} days"
                    item.delete()
                }

            } else {
                println("No suitable for delete.")
            }
        }
    } else {
        println "No build found, skip"
    }

}