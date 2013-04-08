package tachyon.web;

import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import tachyon.CommonUtils;
import tachyon.Constants;
import tachyon.MasterInfo;
import tachyon.Version;
import tachyon.thrift.ClientWorkerInfo;

/**
 * Servlet that provides data for viewing the general status of the filesystem.
 */
public class WebInterfaceGeneralServlet extends HttpServlet {
  private static final long serialVersionUID = 2335205655766736309L;

  private MasterInfo mMasterInfo;

  /**
   * Class to make referencing worker nodes more intuitive. Mainly to avoid implicit association
   * by array indexes.
   */
  public class NodeInfo {
    private final String NAME;
    private final String LAST_CONTACT_SEC;
    private final String STATE;
    private final int FREE_SPACE_PERCENT;
    private final int USED_SPACE_PERCENT;
    private final String UPTIME_CLOCK_TIME;

    private NodeInfo(ClientWorkerInfo workerInfo) {
      NAME = workerInfo.getAddress().getMHost();
      LAST_CONTACT_SEC = Integer.toString(workerInfo.getLastContactSec());
      STATE = workerInfo.getState();
      USED_SPACE_PERCENT = (int) (100L * workerInfo.getUsedBytes() / workerInfo.getCapacityBytes());
      FREE_SPACE_PERCENT = 100 - USED_SPACE_PERCENT;
      UPTIME_CLOCK_TIME = CommonUtils.convertMsToShortClockTime(
          System.currentTimeMillis() - workerInfo.getStarttimeMs());
    }

    public String getName() {
      return NAME;
    }

    public String getLastHeartbeat() {
      return LAST_CONTACT_SEC;
    }

    public String getState() {
      return STATE;
    }

    public int getFreeSpacePercent() {
      return FREE_SPACE_PERCENT;
    }

    public int getUsedSpacePercent() {
      return USED_SPACE_PERCENT;
    }

    public String getUptimeClockTime() {
      return UPTIME_CLOCK_TIME;
    } 
  }

  public WebInterfaceGeneralServlet(MasterInfo masterInfo) {
    mMasterInfo = masterInfo;
  }
  
  /**
   * Redirects the request to a jsp after populating attributes via populateValues.
   * @param request The HttpServletRequest object
   * @param response The HttpServletResponse object
   */  
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) 
      throws ServletException, IOException {
    populateValues(request);
    getServletContext().getRequestDispatcher("/general.jsp").forward(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) {
    return;
  }

  /**
   * Populates key, value pairs for UI display
   * @param request The HttpServletRequest object
   */
  private void populateValues(HttpServletRequest request) {
    request.setAttribute("debug", Constants.DEBUG);

    request.setAttribute("masterNodeAddress", mMasterInfo.getMasterAddress().toString());

    request.setAttribute("uptime", CommonUtils.convertMsToClockTime(
        System.currentTimeMillis() - mMasterInfo.getStarttimeMs()));

    request.setAttribute("startTime", CommonUtils.convertMsToDate(mMasterInfo.getStarttimeMs()));

    request.setAttribute("version", Version.VERSION);

    request.setAttribute("capacity", CommonUtils.getSizeFromBytes(mMasterInfo.getCapacityBytes()));

    request.setAttribute("usedCapacity", 
        CommonUtils.getSizeFromBytes(mMasterInfo.getUsedBytes()));

    request.setAttribute("liveWorkerNodes", Integer.toString(mMasterInfo.getWorkerCount()));

    List<ClientWorkerInfo> workerInfos = mMasterInfo.getWorkersInfo();
    int index = 0;
    NodeInfo[] nodeInfos = new NodeInfo[workerInfos.size()];
    for (ClientWorkerInfo workerInfo : workerInfos) {
      nodeInfos[index ++] = new NodeInfo(workerInfo);
    }
    request.setAttribute("nodeInfos", nodeInfos);

    request.setAttribute("pinlist", mMasterInfo.getPinList());

    request.setAttribute("whitelist", mMasterInfo.getWhiteList());
  }
}