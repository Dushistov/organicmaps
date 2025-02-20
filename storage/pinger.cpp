#include "storage/pinger.hpp"

#include "platform/http_client.hpp"
#include "platform/preferred_languages.hpp"

#include "base/assert.hpp"
#include "base/logging.hpp"
#include "base/stl_helpers.hpp"
#include "base/thread_pool_delayed.hpp"


#include <sstream>
#include <utility>

using namespace std;

namespace
{
auto constexpr kTimeoutInSeconds = 5.0;

void DoPing(string const & url, size_t index, vector<string> & readyUrls)
{
  if (url.empty())
  {
    ASSERT(false, ("Metaserver returned an empty url."));
    return;
  }

  platform::HttpClient request(url);
  request.SetHttpMethod("HEAD");
  request.SetTimeout(kTimeoutInSeconds);
  if (request.RunHttpRequest() && !request.WasRedirected() && request.ErrorCode() == 200)
  {
    readyUrls[index] = url;
  }
  else
  {
    ostringstream ost;
    ost << "Request to server " << url << " failed. Code = " << request.ErrorCode()
        << ", redirection = " << request.WasRedirected();
    LOG(LINFO, (ost.str()));
  }
}
}  // namespace

namespace storage
{
// static
Pinger::Endpoints Pinger::ExcludeUnavailableEndpoints(Endpoints const & urls)
{
  auto const size = urls.size();
  CHECK_GREATER(size, 0, ());

  vector<string> readyUrls(size);
  {
    base::thread_pool::delayed::ThreadPool t(size);
    for (size_t i = 0; i < size; ++i)
      t.Push([url = urls[i], &readyUrls, i] { DoPing(url, i, readyUrls); });

    t.Shutdown(base::thread_pool::delayed::ThreadPool::Exit::ExecPending);
  }

  base::EraseIf(readyUrls, [](auto const & url) { return url.empty(); });
  return readyUrls;
}
}  // namespace storage
