import play.api.http.DefaultHttpFilters
import play.filters.csrf.CSRFFilter
import javax.inject.Inject

class Filters @Inject() (csrfFilter: CSRFFilter)
  extends DefaultHttpFilters(csrfFilter)
