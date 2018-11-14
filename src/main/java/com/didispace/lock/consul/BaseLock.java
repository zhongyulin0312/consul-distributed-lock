package com.didispace.lock.consul;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.session.model.NewSession;
import com.ecwid.consul.v1.session.model.Session;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Data
public class BaseLockUtil {

    protected ConsulClient consulClient;
    protected String sessionId = null;
    protected String keyPath;   // 互斥锁、信号量存储在consul中的基础key路径
    protected CheckTtl checkTtl;  // Check Ttl

    /**
     * @param consulClient
     * @param lockKey       同步锁在consul的KV存储中的Key路径，会自动增加prefix前缀，方便归类查询
     * @param checkTtl      对锁Session的TTL
     */
    protected BaseLock(ConsulClient consulClient, String lockKey, CheckTtl checkTtl) {
        this.consulClient = consulClient;
        this.keyPath = lockKey;
        this.checkTtl = checkTtl;
    }

    /**
     * 创建session
     * @param sessionName
     * @return
     */
    protected String createSession(String sessionName) {
        NewSession newSession = new NewSession();
        newSession.setName(sessionName);
        if(checkTtl != null) {
            checkTtl.start();
            // 如果有CheckTtl，就为该Session设置Check相关信息
            List<String> checks = new ArrayList<>();
            checks.add(checkTtl.getCheckId());
            newSession.setChecks(checks);
            newSession.setBehavior(Session.Behavior.DELETE);
        }
        return consulClient.sessionCreate(newSession, null).getValue();
    }


    /**
     * 根据成员变量sessionId来销毁session
     */
    protected void destroySession() {
        if (sessionId != null) {
            consulClient.sessionDestroy(sessionId, null);
            sessionId = null;
        }
    }



}
