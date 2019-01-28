package com.liushaoming.jseckill.backend.distlock;

import com.liushaoming.jseckill.backend.bean.ZKConfigBean;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class DistributedLockUtil {
    private static Logger logger = LoggerFactory.getLogger(DistributedLockUtil.class);
    public static DistributedLock newDistLock(ZKConfigBean zkConfigBean) {
        try {
            DistributedLock distributedLock = new DistributedLock(zkConfigBean.getLockRoot()
            , zkConfigBean.getConnectStr(), zkConfigBean.getSessionTimeout());
            return distributedLock;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return null;
        } catch (KeeperException e) {
            logger.error(e.getMessage(), e);
            return null;
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
            return null;
        }

    }
}
